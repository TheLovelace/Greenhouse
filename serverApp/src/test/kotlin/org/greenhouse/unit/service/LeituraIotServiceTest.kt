package org.greenhouse.unit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal
import java.time.OffsetDateTime
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.enums.CondicaoAmbiental.ABAIXO_DO_IDEAL
import org.greenhouse.enums.CondicaoAmbiental.ACIMA_DO_IDEAL
import org.greenhouse.enums.CondicaoAmbiental.IDEAL
import org.greenhouse.enums.CondicaoAmbiental.NAO_CONFIGURADO
import org.greenhouse.enums.NivelComplexidade.ALTA
import org.greenhouse.enums.NivelComplexidade.BAIXA
import org.greenhouse.enums.NivelComplexidade.MEDIA
import org.greenhouse.enums.ParametroAmbiental.TEMPERATURA
import org.greenhouse.enums.ParametroAmbiental.UMIDADE_AR
import org.greenhouse.enums.ParametroAmbiental.UMIDADE_SOLO
import org.greenhouse.iot.FilaLeituraIot
import org.greenhouse.model.Alerta
import org.greenhouse.model.DispositivoIot
import org.greenhouse.model.Especie
import org.greenhouse.model.LeituraIot
import org.greenhouse.repository.AlertaRepository
import org.greenhouse.repository.DispositivoIotRepository
import org.greenhouse.repository.LeituraIotRepository
import org.greenhouse.service.LeituraIotService
import org.greenhouse.unit.fixture.MonitoramentoFixture.alerta
import org.greenhouse.unit.fixture.MonitoramentoFixture.dispositivo
import org.greenhouse.unit.fixture.MonitoramentoFixture.especie
import org.greenhouse.unit.fixture.MonitoramentoFixture.payload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

/**
 * Testes de unidade do service LeituraIotService (RF16 - leitura de dados IoT e RF17 -
 * monitoramento por IoT).
 *
 * Recursos de teste usados:
 * - MOCK [fila]: mock da porta FilaLeituraIot. Nos testes de assinatura, o callback registrado pelo
 * service é capturado e chamado à mão, simulando o broker entregando uma mensagem, o teste age como
 * DRIVER da fila.
 * - MOCKS [repository], [dispositivoRepository], [alertaRepository]: substituem o banco.
 * - SPY [objectMapper]: ObjectMapper real do Jackson embrulhado em spy para ser injetado pelo
 * @InjectMocks.
 * - FIXTURE: dispositivo, espécie, regras de alerta e payloads reais de MonitoramentoFixture.
 *
 * Também cobre as regras de avaliação que o service aplica sobre as entidades: conversão da
 * umidade do solo pela calibração do dispositivo, classificação dos valores pela faixa ideal da
 * espécie e cálculo da complexidade de tratamento x preço.
 *
 * Faixa ideal da espécie do fixture: 18–26 °C, ar 50–80 %, solo 40–70 %. Calibração do dispositivo
 * do fixture: raw 3000 = 0 %, raw 1000 = 100 %.
 *
 * Regras de negócio cobertas:
 * - RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
 * umidades), com mínimo ≤ máximo.
 * - RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
 * - RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
 * dados recebidos do IoT.
 * - RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível geração
 * de alertas.
 * - RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
 * monitorados.
 * - RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
 * - RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
 * disponíveis para consulta.
 * - RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %, leitura
 * bruta ≥ 0 e sensor calibrado.
 * - RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
 * monitorada.
 * - RN11: Só é possível consultar ou alterar registros existentes, identificados por id maior que
 * zero.
 */
@ExtendWith(MockitoExtension::class)
class LeituraIotServiceTest {
    @Mock lateinit var repository: LeituraIotRepository

    @Mock lateinit var dispositivoRepository: DispositivoIotRepository

    @Mock lateinit var alertaRepository: AlertaRepository

    @Mock lateinit var fila: FilaLeituraIot

    @Spy var objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

    @InjectMocks lateinit var service: LeituraIotService

    private val topico = "greenhouse/estufa-1/leituras"
    private val idDoDispositivo = 10L
    private val idDaLeitura = 500L

    private val temperaturaMaxima = BigDecimal("26.00")

    private val leituraSoloSeco = 3000
    private val leituraSoloEncharcado = 1000

    private val umidadeSoloZeroPorCento = BigDecimal("0.00")
    private val umidadeSoloCemPorCento = BigDecimal("100.00")

    private val temperaturaMinima = BigDecimal("18.00")
    private val umidadeArMinima = BigDecimal("50.00")
    private val umidadeArMaxima = BigDecimal("80.00")
    private val umidadeSoloMinima = BigDecimal("40.00")
    private val umidadeSoloMaxima = BigDecimal("70.00")

    private val precoMinimoMedia = BigDecimal("30.00")

    private fun especieSemCuidados(
            intervaloRegaDias: Int? = null,
            temperaturaMin: BigDecimal? = null,
            temperaturaMax: BigDecimal? = null,
    ): Especie =
            especie(
                    intervaloRegaDias = intervaloRegaDias,
                    temperaturaMin = temperaturaMin,
                    temperaturaMax = temperaturaMax,
                    umidadeArMin = null,
                    umidadeArMax = null,
                    umidadeSoloMin = null,
                    umidadeSoloMax = null,
            )

    private fun dispositivoNoTopico(
            ativo: Boolean = true,
            calibracaoSeco: Int? = 3000,
    ): DispositivoIot =
            dispositivo(ativo = ativo, calibracaoSeco = calibracaoSeco).also {
                `when`(dispositivoRepository.findByTopicoMqtt(topico)).thenReturn(it)
            }

    private fun bancoSalvaLeituraEGeraId() {
        `when`(repository.save(any(LeituraIot::class.java))).thenAnswer { invocacao ->
            invocacao.getArgument<LeituraIot>(0).also { it.id = idDaLeitura }
        }
    }

    private fun regrasAtivasDoDispositivo(vararg regras: Alerta) {
        `when`(alertaRepository.listarAtivosPorDispositivo(idDoDispositivo))
                .thenReturn(regras.toList())
    }

    private fun dispositivoValidoComRegras(vararg regras: Alerta) {
        dispositivoNoTopico()
        bancoSalvaLeituraEGeraId()
        regrasAtivasDoDispositivo(*regras)
    }

    private fun nenhumaLeituraFoiSalva() =
            verify(repository, never()).save(any(LeituraIot::class.java))

    private fun assinarECapturarCallback(): (String) -> Unit {
        `when`(dispositivoRepository.findByAtivoTrueOrderByIdAsc())
                .thenReturn(listOf(dispositivo(topico = topico)))
        var capturado: ((String) -> Unit)? = null
        doAnswer { invocacao ->
                    capturado = invocacao.getArgument(1)
                    null
                }
                .`when`(fila)
                .assinar(anyString(), any() ?: {})

        service.iniciarMonitoramento()
        return requireNotNull(capturado) { "O service não assinou o tópico" }
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados ainda ativos podem enviar dados e
    // ser monitorados.
    // CT01 - Fluxo principal: o service assina na fila o tópico MQTT de cada dispositivo autorizado
    @Test
    fun `deve assinar na fila o topico de cada dispositivo ativo`() {
        val topicoDaEstufa1 = "greenhouse/estufa-1/leituras"
        val topicoDaEstufa2 = "greenhouse/estufa-2/leituras"
        `when`(dispositivoRepository.findByAtivoTrueOrderByIdAsc())
                .thenReturn(
                        listOf(
                                dispositivo(id = 10, topico = topicoDaEstufa1),
                                dispositivo(id = 11, topico = topicoDaEstufa2)
                        )
                )

        val quantidadeDeTopicosAssinados = service.iniciarMonitoramento()

        assertEquals(2, quantidadeDeTopicosAssinados, "assina um tópico por dispositivo ativo")
        verify(fila, times(1)).assinar(eq(topicoDaEstufa1) ?: "", any() ?: {})
        verify(fila, times(1)).assinar(eq(topicoDaEstufa2) ?: "", any() ?: {})
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT02 - Fluxo alternativo: sem fila configurada o monitoramento não inicia e nada é consultado
    @Test
    fun `nao deve iniciar monitoramento quando a fila nao esta configurada`() {
        val serviceSemFila =
                LeituraIotService(
                        repository,
                        dispositivoRepository,
                        alertaRepository,
                        objectMapper,
                        null
                )

        val quantidadeDeTopicosAssinados = serviceSemFila.iniciarMonitoramento()

        assertEquals(0, quantidadeDeTopicosAssinados, "sem fila nenhum tópico é assinado")
        verifyNoInteractions(dispositivoRepository)
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT03 - Fluxo principal: mensagem entregue pela fila é armazenada
    @Test
    fun `deve armazenar a leitura quando a fila entrega uma mensagem`() {
        val brokerEntregaMensagem = assinarECapturarCallback()
        dispositivoValidoComRegras()

        brokerEntregaMensagem(payload())

        verify(repository, times(1)).save(any(LeituraIot::class.java))
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT04 - Fluxo de exceção: mensagem inválida é descartada sem derrubar a assinatura da fila
    @Test
    fun `deve descartar mensagem invalida da fila sem propagar excecao`() {
        val brokerEntregaMensagem = assinarECapturarCallback()
        dispositivoNoTopico()
        val mensagemQueNaoEJson = "isto não é json"

        assertDoesNotThrow("uma mensagem inválida não pode derrubar a assinatura da fila") {
            brokerEntregaMensagem(mensagemQueNaoEJson)
        }
        nenhumaLeituraFoiSalva()
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT05 - Fluxo principal: leitura armazenada dentro da faixa ideal não gera ocorrência
    @Test
    fun `deve armazenar leitura e nao gerar ocorrencia quando tudo esta no ideal`() {
        dispositivoValidoComRegras(alerta())
        val temperaturaIdeal = BigDecimal("22.50")
        val leituraBrutaDoSolo = 2000
        val leituraEnviadaAoBanco = ArgumentCaptor.forClass(LeituraIot::class.java)

        val resultado =
                service.processarMensagem(
                        topico,
                        payload(
                                temperatura = temperaturaIdeal.toPlainString(),
                                umidadeAr = "65",
                                umidadeSoloRaw = leituraBrutaDoSolo
                        )
                )

        verify(repository).save(leituraEnviadaAoBanco.capture())
        val salva = leituraEnviadaAoBanco.value
        assertAll(
                "leitura armazenada dentro do ideal",
                { assertEquals(temperaturaIdeal, salva.temperatura, "salva a temperatura medida") },
                {
                    assertEquals(
                            BigDecimal("50.00"),
                            salva.umidadeSolo,
                            "salva a umidade do solo já convertida pela calibração"
                    )
                },
                {
                    assertEquals(
                            leituraBrutaDoSolo,
                            salva.umidadeSoloRaw,
                            "guarda também a leitura bruta do sensor"
                    )
                },
                {
                    assertEquals(idDaLeitura, resultado.leituraId, "devolve o id gerado pelo banco")
                },
                {
                    assertEquals(
                            idDoDispositivo,
                            resultado.dispositivoId,
                            "identifica o dispositivo que enviou"
                    )
                },
                {
                    assertTrue(
                            resultado.ocorrencias.isEmpty(),
                            "tudo dentro da faixa: nenhuma ocorrência"
                    )
                },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // CT06 - Fluxo alternativo: temperatura acima da faixa gera ocorrência ACIMA_DO_IDEAL
    @Test
    fun `deve identificar temperatura acima do ideal`() {
        val temperaturaAcimaDoMaximo = BigDecimal("31.00")
        val regraDeAlerta = alerta(id = 100L, prioridade = 3)
        dispositivoValidoComRegras(regraDeAlerta)

        val resultado =
                service.processarMensagem(
                        topico,
                        payload(temperatura = temperaturaAcimaDoMaximo.toPlainString())
                )

        assertEquals(
                1,
                resultado.ocorrencias.size,
                "só a temperatura está fora da faixa: deve haver uma ocorrência"
        )
        val ocorrencia = resultado.ocorrencias.first()
        assertAll(
                "ocorrência de temperatura acima do ideal",
                {
                    assertEquals(
                            TEMPERATURA,
                            ocorrencia.parametro,
                            "o parâmetro fora da faixa é a temperatura"
                    )
                },
                {
                    assertEquals(
                            ACIMA_DO_IDEAL,
                            ocorrencia.condicao,
                            "a temperatura está acima do máximo da espécie"
                    )
                },
                {
                    assertEquals(
                            temperaturaAcimaDoMaximo,
                            ocorrencia.valorMedido,
                            "registra o valor medido pelo sensor"
                    )
                },
                {
                    assertEquals(
                            regraDeAlerta.id,
                            ocorrencia.alertaId,
                            "aponta a regra de alerta que disparou"
                    )
                },
                {
                    assertEquals(
                            regraDeAlerta.prioridade,
                            ocorrencia.prioridade,
                            "herda a prioridade da regra"
                    )
                },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // CT07 - Fluxo alternativo: solo seco (raw 2900 -> 5 %) e ar seco geram duas ocorrências
    // ABAIXO_DO_IDEAL
    @Test
    fun `deve identificar umidade do ar e do solo abaixo do ideal`() {
        dispositivoValidoComRegras(alerta())
        val arSeco = "30"
        val soloQuaseSeco = 2900

        val resultado =
                service.processarMensagem(
                        topico,
                        payload(umidadeAr = arSeco, umidadeSoloRaw = soloQuaseSeco)
                )

        val (ocorrenciaDoAr, ocorrenciaDoSolo) = resultado.ocorrencias
        assertAll(
                "umidades do ar e do solo abaixo do mínimo",
                { assertEquals(2, resultado.ocorrencias.size, "uma ocorrência para cada umidade") },
                {
                    assertEquals(
                            UMIDADE_AR,
                            ocorrenciaDoAr.parametro,
                            "a primeira é a umidade do ar"
                    )
                },
                {
                    assertEquals(
                            ABAIXO_DO_IDEAL,
                            ocorrenciaDoAr.condicao,
                            "o ar está abaixo do mínimo"
                    )
                },
                {
                    assertEquals(
                            UMIDADE_SOLO,
                            ocorrenciaDoSolo.parametro,
                            "a segunda é a umidade do solo"
                    )
                },
                {
                    assertEquals(
                            ABAIXO_DO_IDEAL,
                            ocorrenciaDoSolo.condicao,
                            "o solo está abaixo do mínimo"
                    )
                },
                {
                    assertEquals(
                            BigDecimal("5.00"),
                            ocorrenciaDoSolo.valorMedido,
                            "registra a umidade do solo já convertida em %"
                    )
                },
        )
    }

    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT08 - Fluxo alternativo: parâmetro fora da faixa que a regra não monitora não gera
    // ocorrência
    @Test
    fun `nao deve gerar ocorrencia para parametro nao monitorado pela regra`() {
        val regraQueSoMonitoraOSolo =
                alerta(
                        monitorarTemperatura = false,
                        monitorarUmidadeAr = false,
                        monitorarUmidadeSolo = true
                )
        dispositivoValidoComRegras(regraQueSoMonitoraOSolo)
        val temperaturaMuitoAlta = "40.00"

        val resultado =
                service.processarMensagem(topico, payload(temperatura = temperaturaMuitoAlta))

        assertTrue(
                resultado.ocorrencias.isEmpty(),
                "a temperatura está alta, mas a regra não monitora temperatura"
        )
    }

    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // CT09 - Fluxo alternativo: regras de espécie inativa são ignoradas na avaliação
    @Test
    fun `deve ignorar regras de especie inativa`() {
        val regraDeEspecieInativa = alerta(especie = especie(ativo = false))
        dispositivoValidoComRegras(regraDeEspecieInativa)
        val temperaturaMuitoAlta = "40.00"

        val resultado =
                service.processarMensagem(topico, payload(temperatura = temperaturaMuitoAlta))

        assertTrue(
                resultado.ocorrencias.isEmpty(),
                "espécie inativa não é monitorada, mesmo com temperatura fora da faixa"
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // CT10 - Fluxo alternativo: cada regra é avaliada com a faixa da sua própria espécie
    @Test
    fun `deve avaliar cada regra com a faixa da propria especie`() {
        val regraDaTropical = alerta(id = 1, especie = especie(id = 1))
        val regraDoCacto =
                alerta(
                        id = 2,
                        especie =
                                especie(
                                        id = 2,
                                        temperaturaMin = BigDecimal("10"),
                                        temperaturaMax = BigDecimal("35")
                                )
                )
        dispositivoValidoComRegras(regraDaTropical, regraDoCacto)
        val temperaturaBoaSoParaOCacto = "30.00"

        val resultado =
                service.processarMensagem(topico, payload(temperatura = temperaturaBoaSoParaOCacto))

        assertEquals(
                listOf(regraDaTropical.id),
                resultado.ocorrencias.map { it.alertaId },
                "30 °C só está fora da faixa da tropical, o cacto não gera ocorrência"
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // CT11 - Análise de valor limite: temperatura igual ao máximo (26,00) é ideal, 26,01 já gera
    // ocorrência
    @Test
    fun `deve respeitar o limite maximo de temperatura de forma inclusiva`() {
        dispositivoValidoComRegras(alerta())
        val exatamenteNoMaximo = temperaturaMaxima
        val umCentesimoAcimaDoMaximo = temperaturaMaxima + BigDecimal("0.01")

        val noMaximo =
                service.processarMensagem(
                        topico,
                        payload(temperatura = exatamenteNoMaximo.toPlainString())
                )
        val acimaDoMaximo =
                service.processarMensagem(
                        topico,
                        payload(temperatura = umCentesimoAcimaDoMaximo.toPlainString())
                )

        assertAll(
                "limite máximo de temperatura (26 °C)",
                { assertTrue(noMaximo.ocorrencias.isEmpty(), "exatamente 26,00 °C ainda é ideal") },
                { assertEquals(1, acimaDoMaximo.ocorrencias.size, "26,01 °C já gera ocorrência") },
        )
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT12 - Fluxo alternativo: usa a data enviada pelo dispositivo quando presente
    @Test
    fun `deve usar a data de leitura informada no payload`() {
        dispositivoValidoComRegras()
        val dataEnviadaPeloDispositivo = "2026-11-09T10:15:00-03:00"

        val resultado =
                service.processarMensagem(topico, payload(dataLeitura = dataEnviadaPeloDispositivo))

        assertTrue(
                OffsetDateTime.parse(dataEnviadaPeloDispositivo).isEqual(resultado.dataLeitura),
                "a leitura fica com a data informada pelo dispositivo"
        )
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT13 - Fluxo alternativo: sem data no payload, registra o horário de recebimento
    @Test
    fun `deve registrar o horario atual quando o payload nao traz data`() {
        dispositivoValidoComRegras()
        val antesDeReceber = OffsetDateTime.now()

        val resultado = service.processarMensagem(topico, payload(dataLeitura = null))

        assertFalse(
                resultado.dataLeitura.isBefore(antesDeReceber),
                "sem data no payload, a leitura recebe o horário do recebimento"
        )
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT14 - Fluxo de exceção: leitura de tópico sem dispositivo cadastrado é rejeitada
    @Test
    fun `nao deve armazenar leitura de topico sem dispositivo cadastrado`() {
        val topicoDesconhecido = "desconhecido"
        `when`(dispositivoRepository.findByTopicoMqtt(topicoDesconhecido)).thenReturn(null)

        val erro =
                assertThrows(NotFoundException::class.java) {
                    service.processarMensagem(topicoDesconhecido, payload())
                }

        assertEquals("Dispositivo IoT não cadastrado para o tópico informado", erro.message)
        nenhumaLeituraFoiSalva()
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT15 - Fluxo de exceção: leitura de dispositivo não autorizado (inativo) é rejeitada
    @Test
    fun `nao deve armazenar leitura de dispositivo inativo`() {
        dispositivoNoTopico(ativo = false)

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.processarMensagem(topico, payload())
                }

        assertEquals("O dispositivo IoT não está autorizado a enviar dados", erro.message)
        nenhumaLeituraFoiSalva()
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT16 - Fluxo de exceção: payload que não é JSON válido é rejeitado
    @Test
    fun `nao deve armazenar leitura com payload invalido`() {
        dispositivoNoTopico()
        val jsonCortadoNoMeio = "{\"temperatura\":"

        val erro =
                assertThrows(BadRequestException::class.java) {
                    service.processarMensagem(topico, jsonCortadoNoMeio)
                }

        assertAll(
                "payload inválido",
                {
                    assertEquals(
                            "Payload da leitura IoT inválido",
                            erro.message,
                            "mensagem de erro"
                    )
                },
                {
                    assertNotNull(erro.cause, "preserva o erro original do parser para diagnóstico")
                },
        )
        nenhumaLeituraFoiSalva()
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT17 - Análise de valor limite: umidade do ar -0,01 e 100,01 ficam fora do intervalo 0–100
    @Test
    fun `nao deve armazenar leitura com umidade do ar fora de 0 a 100`() {
        dispositivoNoTopico()
        val logoAbaixoDeZero = "-0.01"
        val logoAcimaDeCem = "100.01"

        val erroAbaixo =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.processarMensagem(topico, payload(umidadeAr = logoAbaixoDeZero))
                }
        val erroAcima =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.processarMensagem(topico, payload(umidadeAr = logoAcimaDeCem))
                }

        val mensagemEsperada = "A umidade do ar deve estar entre 0 e 100"
        assertAll(
                "umidade do ar fora de 0–100 %",
                { assertEquals(mensagemEsperada, erroAbaixo.message, "-0,01 % é rejeitado") },
                { assertEquals(mensagemEsperada, erroAcima.message, "100,01 % é rejeitado") },
        )
        nenhumaLeituraFoiSalva()
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT18 - Análise de valor limite: umidade do ar 0 e 100 (extremos válidos) são aceitas
    @Test
    fun `deve armazenar leitura com umidade do ar nos extremos 0 e 100`() {
        dispositivoValidoComRegras()
        val zeroPorCento = BigDecimal("0")
        val cemPorCento = BigDecimal("100")

        val comZero =
                service.processarMensagem(topico, payload(umidadeAr = zeroPorCento.toPlainString()))
        val comCem =
                service.processarMensagem(topico, payload(umidadeAr = cemPorCento.toPlainString()))

        assertAll(
                "extremos válidos da umidade do ar",
                { assertEquals(zeroPorCento, comZero.umidadeAr, "0 % é aceito") },
                { assertEquals(cemPorCento, comCem.umidadeAr, "100 % é aceito") },
        )
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT19 - Fluxo de exceção: leitura bruta negativa do sensor de solo é rejeitada
    @Test
    fun `nao deve armazenar leitura com umidade do solo bruta negativa`() {
        dispositivoNoTopico()
        val leituraBrutaNegativa = -1

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.processarMensagem(
                            topico,
                            payload(umidadeSoloRaw = leituraBrutaNegativa)
                    )
                }

        assertEquals("A leitura bruta de umidade do solo não pode ser negativa", erro.message)
        nenhumaLeituraFoiSalva()
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT20 - Fluxo de exceção: leitura de dispositivo sem calibração é rejeitada
    @Test
    fun `nao deve armazenar leitura de dispositivo sem calibracao`() {
        dispositivoNoTopico(calibracaoSeco = null)

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.processarMensagem(topico, payload())
                }

        assertEquals(
                "O dispositivo IoT não possui calibração de umidade do solo válida",
                erro.message
        )
        nenhumaLeituraFoiSalva()
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // CT21 - Fluxo principal: categoriza cada grandeza da leitura pela faixa da espécie
    @Test
    fun `deve categorizar cada parametro da leitura pela faixa da especie`() {
        val especieSemFaixaDeUmidadeAr = especie(umidadeArMin = null, umidadeArMax = null)
        val leitura =
                LeituraIot(
                        dispositivo = dispositivo(),
                        temperatura = BigDecimal("15.00"),
                        umidadeAr = BigDecimal("65.00"),
                        umidadeSolo = BigDecimal("75.00"),
                )

        val condicoes = service.avaliarCondicoes(especieSemFaixaDeUmidadeAr, leitura)

        assertAll(
                "categorização de cada parâmetro",
                {
                    assertEquals(
                            ABAIXO_DO_IDEAL,
                            condicoes[TEMPERATURA],
                            "15 °C está abaixo do mínimo de 18 °C"
                    )
                },
                {
                    assertEquals(
                            NAO_CONFIGURADO,
                            condicoes[UMIDADE_AR],
                            "sem faixa de umidade do ar não há como avaliar"
                    )
                },
                {
                    assertEquals(
                            ACIMA_DO_IDEAL,
                            condicoes[UMIDADE_SOLO],
                            "75 % está acima do máximo de 70 %"
                    )
                },
        )
        verifyNoInteractions(repository, dispositivoRepository, alertaRepository)
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT22 - Fluxo principal: consulta uma leitura armazenada pelo id
    @Test
    fun `deve buscar leitura por id`() {
        val leituraArmazenada = LeituraIot(id = idDaLeitura, dispositivo = dispositivo())
        `when`(repository.buscarPorIdComDispositivo(idDaLeitura)).thenReturn(leituraArmazenada)

        val resultado = service.buscarPorId(idDaLeitura)

        assertSame(leituraArmazenada, resultado, "devolve a leitura do id pedido")
    }

    // RN11: Só é possível consultar ou alterar registros existentes, identificados por id maior que
    // zero.
    // CT23 - Fluxo de exceção: id 0 e leitura inexistente são rejeitados
    @Test
    fun `deve rejeitar id invalido e leitura inexistente`() {
        val idInvalido = 0L
        val idInexistente = 9L
        `when`(repository.buscarPorIdComDispositivo(idInexistente)).thenReturn(null)

        val erroIdInvalido =
                assertThrows(BadRequestException::class.java) { service.buscarPorId(idInvalido) }
        val erroInexistente =
                assertThrows(NotFoundException::class.java) { service.buscarPorId(idInexistente) }

        assertAll(
                "consulta de leitura inválida",
                {
                    assertEquals(
                            "O ID da leitura deve ser maior que zero",
                            erroIdInvalido.message,
                            "id 0 é rejeitado antes de consultar o banco"
                    )
                },
                {
                    assertEquals(
                            "Leitura IoT não encontrada",
                            erroInexistente.message,
                            "id sem leitura armazenada é rejeitado"
                    )
                },
        )
        verify(repository, never()).buscarPorIdComDispositivo(idInvalido)
        verify(repository, times(1)).buscarPorIdComDispositivo(anyLong())
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT24 - Fluxo principal: consulta o histórico de leituras de todos os dispositivos ou de um só
    @Test
    fun `deve listar leituras desde uma data com ou sem filtro de dispositivo`() {
        val desde = OffsetDateTime.parse("2026-11-01T00:00:00Z")
        val leiturasDeTodos = listOf(mock(LeituraIot::class.java), mock(LeituraIot::class.java))
        val leiturasDoDispositivo = listOf(mock(LeituraIot::class.java))
        `when`(repository.listarDesde(desde)).thenReturn(leiturasDeTodos)
        `when`(repository.listarDesdePorDispositivo(idDoDispositivo, desde))
                .thenReturn(leiturasDoDispositivo)

        val semFiltro = service.listarDesde(desde)
        val comFiltro = service.listarDesde(desde, dispositivoId = idDoDispositivo)

        assertAll(
                "histórico de leituras",
                {
                    assertEquals(
                            leiturasDeTodos.size,
                            semFiltro.size,
                            "sem filtro traz as leituras de todos os dispositivos"
                    )
                },
                {
                    assertEquals(
                            leiturasDoDispositivo.size,
                            comFiltro.size,
                            "com filtro traz só as leituras do dispositivo"
                    )
                },
        )
        verify(repository, times(1)).listarDesde(desde)
        verify(repository, times(1)).listarDesdePorDispositivo(idDoDispositivo, desde)
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT25 - Fluxo principal: leitura bruta intermediária vira percentual proporcional à calibração
    @Test
    fun `deve converter leitura bruta intermediaria em percentual`() {
        val sensor = dispositivo()
        val leituraNoMeioDaCalibracao = 2000
        val leituraAUmQuartoDaCalibracao = 2500

        assertAll(
                "conversão proporcional à calibração",
                {
                    assertEquals(
                            BigDecimal("50.00"),
                            service.converterUmidadeSolo(sensor, leituraNoMeioDaCalibracao),
                            "o meio da calibração corresponde a 50 % de umidade"
                    )
                },
                {
                    assertEquals(
                            BigDecimal("25.00"),
                            service.converterUmidadeSolo(sensor, leituraAUmQuartoDaCalibracao),
                            "um quarto da calibração corresponde a 25 % de umidade"
                    )
                },
        )
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT26 - Análise de valor limite: os pontos de calibração seco e úmido viram 0 % e 100 %
    @Test
    fun `deve converter os pontos de calibracao em 0 e 100 por cento`() {
        val sensor = dispositivo()

        assertAll(
                "pontos de calibração",
                {
                    assertEquals(
                            umidadeSoloZeroPorCento,
                            service.converterUmidadeSolo(sensor, leituraSoloSeco),
                            "a leitura do solo seco corresponde a 0 %"
                    )
                },
                {
                    assertEquals(
                            umidadeSoloCemPorCento,
                            service.converterUmidadeSolo(sensor, leituraSoloEncharcado),
                            "a leitura do solo encharcado corresponde a 100 %"
                    )
                },
        )
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT27 - Fluxo alternativo: leituras fora da calibração são limitadas ao intervalo 0–100 %
    @Test
    fun `deve limitar ao intervalo de 0 a 100 leituras fora da calibracao`() {
        val sensor = dispositivo()
        val leituraMaisSecaQueACalibracao = 3500
        val leituraMaisUmidaQueACalibracao = 500

        assertAll(
                "limites do percentual",
                {
                    assertEquals(
                            umidadeSoloZeroPorCento,
                            service.converterUmidadeSolo(sensor, leituraMaisSecaQueACalibracao),
                            "leitura mais seca que a calibração não fica negativa"
                    )
                },
                {
                    assertEquals(
                            umidadeSoloCemPorCento,
                            service.converterUmidadeSolo(sensor, leituraMaisUmidaQueACalibracao),
                            "leitura mais úmida que a calibração não passa de 100 %"
                    )
                },
        )
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT28 - Fluxo de exceção: dispositivo sem calibração não converte a leitura
    @Test
    fun `nao deve converter leitura quando o dispositivo nao tem calibracao`() {
        val sensorSemCalibracaoSeca = dispositivo(calibracaoSeco = null)
        val sensorSemCalibracaoUmida = dispositivo(calibracaoUmido = null)

        val semSeca =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.converterUmidadeSolo(sensorSemCalibracaoSeca, 2000)
                }
        val semUmida =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.converterUmidadeSolo(sensorSemCalibracaoUmida, 2000)
                }

        val mensagemEsperada = "O dispositivo IoT não possui calibração de umidade do solo válida"
        assertAll(
                "dispositivo sem calibração",
                { assertEquals(mensagemEsperada, semSeca.message, "falta o ponto de solo seco") },
                { assertEquals(mensagemEsperada, semUmida.message, "falta o ponto de solo úmido") },
        )
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT29 - Fluxo de exceção: calibração com seco = úmido causaria divisão por zero
    @Test
    fun `nao deve converter leitura quando a calibracao seca e umida sao iguais`() {
        val sensorComCalibracaoInvalida = dispositivo(calibracaoSeco = 2000, calibracaoUmido = 2000)

        assertThrows(
                UnprocessableEntityException::class.java,
                { service.converterUmidadeSolo(sensorComCalibracaoInvalida, 2000) },
                "calibração sem intervalo entre seco e úmido não permite converter a leitura"
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // CT30 - Fluxo principal: valor dentro da faixa é categorizado como IDEAL
    @Test
    fun `deve classificar como ideal valor dentro da faixa`() {
        val temperaturaDentroDaFaixa = BigDecimal("22.00")

        val condicao = service.classificar(especie(), TEMPERATURA, temperaturaDentroDaFaixa)

        assertAll(
                "temperatura dentro da faixa 18–26 °C",
                { assertEquals(IDEAL, condicao, "22 °C está dentro da faixa ideal") },
                { assertFalse(condicao.foraDoLimite, "condição ideal não deve gerar alerta") },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // CT31 - Fluxo alternativo: valor abaixo do mínimo é ABAIXO_DO_IDEAL e fica marcado como fora
    // do limite
    @Test
    fun `deve classificar como abaixo do ideal valor menor que o minimo`() {
        val umidadeArUmCentesimoAbaixo = umidadeArMinima - BigDecimal("0.01")

        val condicao = service.classificar(especie(), UMIDADE_AR, umidadeArUmCentesimoAbaixo)

        assertAll(
                "umidade do ar abaixo do mínimo de 50 %",
                { assertEquals(ABAIXO_DO_IDEAL, condicao, "49,99 % está abaixo do mínimo") },
                { assertTrue(condicao.foraDoLimite, "deve ficar marcada para gerar alerta") },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // CT32 - Fluxo alternativo: valor acima do máximo é ACIMA_DO_IDEAL e fica marcado como fora do
    // limite
    @Test
    fun `deve classificar como acima do ideal valor maior que o maximo`() {
        val umidadeSoloUmCentesimoAcima = umidadeSoloMaxima + BigDecimal("0.01")

        val condicao = service.classificar(especie(), UMIDADE_SOLO, umidadeSoloUmCentesimoAcima)

        assertAll(
                "umidade do solo acima do máximo de 70 %",
                { assertEquals(ACIMA_DO_IDEAL, condicao, "70,01 % está acima do máximo") },
                { assertTrue(condicao.foraDoLimite, "deve ficar marcada para gerar alerta") },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // CT33 - Análise de valor limite: os limites mínimo e máximo da faixa são inclusivos
    @Test
    fun `deve classificar como ideal valores exatamente nos limites da faixa`() {
        val especie = especie()

        assertAll(
                "limites da faixa de temperatura são inclusivos",
                {
                    assertEquals(
                            IDEAL,
                            service.classificar(especie, TEMPERATURA, temperaturaMinima),
                            "exatamente o mínimo (18 °C) ainda é ideal"
                    )
                },
                {
                    assertEquals(
                            IDEAL,
                            service.classificar(especie, TEMPERATURA, temperaturaMaxima),
                            "exatamente o máximo (26 °C) ainda é ideal"
                    )
                },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // CT34 - Fluxo de exceção: espécie sem faixa configurada não pode ser avaliada
    // (NAO_CONFIGURADO)
    @Test
    fun `deve retornar nao configurado quando a especie nao tem faixa para o parametro`() {
        val especieSemFaixaDeUmidadeAr = especie(umidadeArMin = null, umidadeArMax = null)

        val condicao = service.classificar(especieSemFaixaDeUmidadeAr, UMIDADE_AR, BigDecimal("10.00"))

        assertAll(
                "parâmetro sem faixa configurada",
                { assertEquals(NAO_CONFIGURADO, condicao, "sem faixa não há como avaliar") },
                { assertFalse(condicao.foraDoLimite, "sem faixa não deve gerar alerta") },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // CT35 - Fluxo alternativo: faixa aberta com só o mínimo configurado
    @Test
    fun `deve avaliar apenas o minimo quando o maximo nao esta configurado`() {
        val especieSemMaximo = especie(temperaturaMax = null)

        assertAll(
                "faixa com só a temperatura mínima (18 °C)",
                {
                    assertEquals(
                            ABAIXO_DO_IDEAL,
                            service.classificar(especieSemMaximo, TEMPERATURA, BigDecimal("10")),
                            "abaixo do mínimo continua sendo detectado"
                    )
                },
                {
                    assertEquals(
                            IDEAL,
                            service.classificar(especieSemMaximo, TEMPERATURA, BigDecimal("45")),
                            "sem máximo, qualquer valor alto é aceito"
                    )
                },
        )
    }

    // RN03: Os parâmetros configurados na espécie devem ser usados para avaliar e categorizar os
    // dados recebidos do IoT.
    // CT36 - Fluxo alternativo: faixa aberta com só o máximo configurado
    @Test
    fun `deve avaliar apenas o maximo quando o minimo nao esta configurado`() {
        val especieSemMinimo = especie(temperaturaMin = null)

        assertAll(
                "faixa com só a temperatura máxima (26 °C)",
                {
                    assertEquals(
                            IDEAL,
                            service.classificar(especieSemMinimo, TEMPERATURA, BigDecimal("-5")),
                            "sem mínimo, qualquer valor baixo é aceito"
                    )
                },
                {
                    assertEquals(
                            ACIMA_DO_IDEAL,
                            service.classificar(especieSemMinimo, TEMPERATURA, BigDecimal("30")),
                            "acima do máximo continua sendo detectado"
                    )
                },
        )
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT37 - Fluxo principal: devolve a faixa configurada para cada parâmetro ambiental
    @Test
    fun `deve devolver a faixa configurada de cada parametro ambiental`() {
        val especie = especie()

        assertAll(
                "faixas de cuidado da espécie",
                {
                    assertEquals(
                            temperaturaMinima to temperaturaMaxima,
                            service.faixaDe(especie, TEMPERATURA),
                            "faixa de temperatura"
                    )
                },
                {
                    assertEquals(
                            umidadeArMinima to umidadeArMaxima,
                            service.faixaDe(especie, UMIDADE_AR),
                            "faixa de umidade do ar"
                    )
                },
                {
                    assertEquals(
                            umidadeSoloMinima to umidadeSoloMaxima,
                            service.faixaDe(especie, UMIDADE_SOLO),
                            "faixa de umidade do solo"
                    )
                },
        )
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT38 - Fluxo principal: rega espaçada e faixas amplas resultam em complexidade BAIXA
    @Test
    fun `deve calcular complexidade baixa para especie de cuidado simples`() {
        val cacto =
                especie(
                        intervaloRegaDias = 15,
                        temperaturaMin = BigDecimal("10"),
                        temperaturaMax = BigDecimal("35"),
                        umidadeArMin = BigDecimal("10"),
                        umidadeArMax = BigDecimal("60"),
                        umidadeSoloMin = BigDecimal("5"),
                        umidadeSoloMax = BigDecimal("40"),
                )

        assertEquals(BAIXA, service.nivelComplexidade(cacto), "0 pontos de cuidado = complexidade baixa")
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT39 - Fluxo alternativo: sem cuidados configurados não há pontuação: complexidade BAIXA
    @Test
    fun `deve calcular complexidade baixa quando nenhum cuidado esta configurado`() {
        assertEquals(
                BAIXA,
                service.nivelComplexidade(especieSemCuidados()),
                "sem cuidados configurados não há pontos de complexidade"
        )
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT40 - Fluxo principal: rega a cada 3 dias (1 ponto) + amplitude térmica de 8 °C (1 ponto) =
    // MEDIA
    @Test
    fun `deve calcular complexidade media para especie de cuidado moderado`() {
        val especiePadrao = especie()

        assertEquals(
                MEDIA,
                service.nivelComplexidade(especiePadrao),
                "2 pontos de cuidado = complexidade média"
        )
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT41 - Fluxo principal: rega diária (2) + faixa térmica estreita (2) + umidades estreitas
    // (1+1) = ALTA
    @Test
    fun `deve calcular complexidade alta para especie de cuidado exigente`() {
        val orquidea =
                especie(
                        intervaloRegaDias = 1,
                        temperaturaMin = BigDecimal("20"),
                        temperaturaMax = BigDecimal("24"),
                        umidadeArMin = BigDecimal("60"),
                        umidadeArMax = BigDecimal("75"),
                        umidadeSoloMin = BigDecimal("50"),
                        umidadeSoloMax = BigDecimal("60"),
                )

        assertEquals(ALTA, service.nivelComplexidade(orquidea), "6 pontos de cuidado = complexidade alta")
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT42 - Análise de valor limite: rega de 2 dias = 2 pontos, 3 e 7 dias = 1 ponto, 8 dias = 0
    @Test
    fun `deve pontuar a rega respeitando os limites de 2 e 7 dias`() {
        fun complexidadeComRegaACada(dias: Int) =
                service.nivelComplexidade(especieSemCuidados(intervaloRegaDias = dias))

        assertAll(
                "pontuação da frequência de rega",
                { assertEquals(MEDIA, complexidadeComRegaACada(2), "2 dias ainda vale 2 pontos") },
                { assertEquals(BAIXA, complexidadeComRegaACada(3), "3 dias já vale só 1 ponto") },
                { assertEquals(BAIXA, complexidadeComRegaACada(7), "7 dias ainda vale 1 ponto") },
                { assertEquals(BAIXA, complexidadeComRegaACada(8), "8 dias não pontua") },
        )
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT43 - Análise de valor limite: amplitude térmica < 5 = 2 pontos, 5 = 1 ponto, 10 = 0
    @Test
    fun `deve pontuar a amplitude termica respeitando os limites de 5 e 10 graus`() {
        fun complexidadeComAmplitudeTermica(amplitude: String) =
                service.nivelComplexidade(
                        especieSemCuidados(
                                temperaturaMin = BigDecimal("20"),
                                temperaturaMax = BigDecimal("20") + BigDecimal(amplitude),
                        )
                )

        assertAll(
                "pontuação da amplitude térmica",
                {
                    assertEquals(
                            MEDIA,
                            complexidadeComAmplitudeTermica("4.99"),
                            "abaixo de 5 °C vale 2 pontos"
                    )
                },
                {
                    assertEquals(
                            BAIXA,
                            complexidadeComAmplitudeTermica("5"),
                            "exatamente 5 °C já vale só 1 ponto"
                    )
                },
                {
                    assertEquals(
                            BAIXA,
                            complexidadeComAmplitudeTermica("10"),
                            "10 °C ou mais não pontua"
                    )
                },
        )
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT44 - Análise de valor limite: preço igual ou acima do mínimo do nível de complexidade é
    // aceito
    @Test
    fun `deve aceitar preco igual ou acima do minimo da complexidade`() {
        val especieDeComplexidadeMedia = especie()

        assertAll(
                "preço compatível com complexidade média (mínimo R$ 30,00)",
                {
                    assertTrue(
                            service.precoCompativel(especieDeComplexidadeMedia, precoMinimoMedia),
                            "exatamente o preço mínimo é aceito"
                    )
                },
                {
                    assertTrue(
                            service.precoCompativel(especieDeComplexidadeMedia, BigDecimal("59.90")),
                            "preço acima do mínimo é aceito"
                    )
                },
        )
    }

    // RN02: O preço dos produtos deve corresponder à complexidade de tratamento da espécie.
    // CT45 - Análise de valor limite: preço um centavo abaixo do mínimo do nível de complexidade é
    // rejeitado
    @Test
    fun `deve rejeitar preco abaixo do minimo da complexidade`() {
        val especieDeComplexidadeMedia = especie()
        val umCentavoAbaixoDoMinimo = precoMinimoMedia - BigDecimal("0.01")

        assertFalse(
                service.precoCompativel(especieDeComplexidadeMedia, umCentavoAbaixoDoMinimo),
                "R$ 29,99 não cobre a complexidade média (mínimo R$ 30,00)"
        )
    }
}
