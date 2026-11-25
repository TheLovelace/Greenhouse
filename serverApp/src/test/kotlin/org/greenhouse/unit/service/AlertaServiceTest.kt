package org.greenhouse.unit.service

import java.util.Optional
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.mapper.AlertaMapper
import org.greenhouse.model.Alerta
import org.greenhouse.model.DispositivoIot
import org.greenhouse.model.Especie
import org.greenhouse.repository.AlertaRepository
import org.greenhouse.repository.DispositivoIotRepository
import org.greenhouse.repository.EspecieRepository
import org.greenhouse.service.AlertaService
import org.greenhouse.unit.fixture.MonitoramentoFixture.alerta
import org.greenhouse.unit.fixture.MonitoramentoFixture.alertaRequest
import org.greenhouse.unit.fixture.MonitoramentoFixture.dispositivo
import org.greenhouse.unit.fixture.MonitoramentoFixture.especie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

/**
 * Testes de unidade do service AlertaService: configuração das regras de monitoramento (qual
 * dispositivo IoT observa qual espécie e quais condições geram alerta).
 *
 * Recursos de teste usados:
 * - MOCKS [repository], [dispositivoRepository], [especieRepository]: substituem o banco.
 * - SPY [mapper]: AlertaMapper real, injetado no service pelo @InjectMocks.
 * - FIXTURE: dispositivos, espécies e alertas reais vindos de MonitoramentoFixture.
 *
 * Regras de negócio cobertas:
 * - RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
 * monitorados.
 * - RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
 * - RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
 * disponíveis para consulta.
 * - RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
 * monitorada.
 * - RN11: Só é possível consultar ou alterar registros existentes, identificados por id maior que
 * zero.
 */
@ExtendWith(MockitoExtension::class)
class AlertaServiceTest {
    @Mock lateinit var repository: AlertaRepository

    @Mock lateinit var dispositivoRepository: DispositivoIotRepository

    @Mock lateinit var especieRepository: EspecieRepository

    @Spy var mapper = AlertaMapper()

    @InjectMocks lateinit var service: AlertaService

    private val idDoDispositivo = 10L
    private val idDaEspecie = 1L
    private val idDaRegra = 100L

    private fun dispositivoEEspecieCadastrados(
            dispositivoAtivo: Boolean = true,
            especieAtiva: Boolean = true,
    ) {
        dispositivoCadastrado(dispositivo(ativo = dispositivoAtivo))
        especieCadastrada(especie(ativo = especieAtiva))
    }

    private fun dispositivoCadastrado(dispositivo: DispositivoIot) {
        `when`(dispositivoRepository.findById(requireNotNull(dispositivo.id)))
                .thenReturn(Optional.of(dispositivo))
    }

    private fun especieCadastrada(especie: Especie) {
        `when`(especieRepository.findById(requireNotNull(especie.id)))
                .thenReturn(Optional.of(especie))
    }

    private fun regraCadastrada(regra: Alerta): Alerta {
        `when`(repository.buscarComRelacionamentos(requireNotNull(regra.id))).thenReturn(regra)
        return regra
    }

    private fun bancoSalvaEGeraId() {
        `when`(repository.save(any(Alerta::class.java))).thenAnswer { invocacao ->
            invocacao.getArgument<Alerta>(0).also { if (it.id == null) it.id = idDaRegra }
        }
    }

    private fun nenhumaRegraFoiSalva() = verify(repository, never()).save(any(Alerta::class.java))

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT01 - Fluxo principal: regra válida para dispositivo e espécie ativos é cadastrada
    @Test
    fun `deve cadastrar regra de monitoramento com sucesso`() {
        dispositivoEEspecieCadastrados()
        bancoSalvaEGeraId()
        val novaRegra = alertaRequest()

        val resultado = service.cadastrar(novaRegra)

        assertAll(
                "regra de monitoramento cadastrada",
                { assertEquals(idDaRegra, resultado.id, "devolve o id gerado pelo banco") },
                {
                    assertEquals(
                            "Condições da samambaia",
                            resultado.titulo,
                            "o título é salvo sem espaços nas pontas"
                    )
                },
                {
                    assertEquals(
                            idDoDispositivo,
                            resultado.dispositivoIotId,
                            "a regra fica ligada ao dispositivo informado"
                    )
                },
                {
                    assertEquals(
                            idDaEspecie,
                            resultado.especieId,
                            "a regra fica ligada à espécie informada"
                    )
                },
                {
                    assertTrue(
                            resultado.monitorarTemperatura,
                            "a condição escolhida (temperatura) é monitorada"
                    )
                },
        )
        verify(repository, times(1)).save(any(Alerta::class.java))
    }

    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT02 - Fluxo de exceção: regra sem nenhuma condição monitorada é rejeitada
    @Test
    fun `nao deve cadastrar regra sem condicao monitorada`() {
        val regraSemCondicao =
                alertaRequest(
                        monitorarTemperatura = false,
                        monitorarUmidadeAr = false,
                        monitorarUmidadeSolo = false
                )

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(regraSemCondicao)
                }

        assertEquals("Selecione ao menos uma condição para monitorar", erro.message)
        nenhumaRegraFoiSalva()
    }

    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT03 - Fluxo alternativo: monitorar uma única condição já é suficiente
    @Test
    fun `deve cadastrar regra que monitora apenas umidade do solo`() {
        dispositivoEEspecieCadastrados()
        bancoSalvaEGeraId()
        val soUmidadeDoSolo =
                alertaRequest(monitorarTemperatura = false, monitorarUmidadeSolo = true)

        val resultado = service.cadastrar(soUmidadeDoSolo)

        assertAll(
                "regra com uma única condição",
                { assertFalse(resultado.monitorarTemperatura, "a temperatura não é monitorada") },
                { assertTrue(resultado.monitorarUmidadeSolo, "a umidade do solo é monitorada") },
        )
    }

    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT04 - Fluxo de exceção: regra com título em branco é rejeitada
    @Test
    fun `nao deve cadastrar regra com titulo em branco`() {
        val tituloSoComEspacos = alertaRequest(titulo = "   ")

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(tituloSoComEspacos)
                }

        assertEquals("O título do alerta é obrigatório", erro.message)
    }

    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT05 - Análise de valor limite: prioridades 0 e 4 ficam fora do intervalo 1..3
    @Test
    fun `nao deve cadastrar regra com prioridade fora do intervalo`() {
        val logoAbaixoDoMinimo = 0
        val logoAcimaDoMaximo = 4

        val erroAbaixo =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(alertaRequest(prioridade = logoAbaixoDoMinimo))
                }
        val erroAcima =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(alertaRequest(prioridade = logoAcimaDoMaximo))
                }

        val mensagemEsperada = "A prioridade deve ser 1, 2 ou 3"
        assertAll(
                "prioridade fora de 1..3",
                { assertEquals(mensagemEsperada, erroAbaixo.message, "prioridade 0 é rejeitada") },
                { assertEquals(mensagemEsperada, erroAcima.message, "prioridade 4 é rejeitada") },
        )
        nenhumaRegraFoiSalva()
    }

    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT06 - Análise de valor limite: prioridades 1 e 3 (extremos válidos) são aceitas
    @Test
    fun `deve cadastrar regra com prioridades nos extremos validos`() {
        dispositivoEEspecieCadastrados()
        bancoSalvaEGeraId()
        val prioridadeMinima = 1
        val prioridadeMaxima = 3

        val comPrioridadeMinima = service.cadastrar(alertaRequest(prioridade = prioridadeMinima))
        val comPrioridadeMaxima = service.cadastrar(alertaRequest(prioridade = prioridadeMaxima))

        assertAll(
                "prioridades nos extremos de 1..3",
                {
                    assertEquals(
                            prioridadeMinima,
                            comPrioridadeMinima.prioridade,
                            "prioridade 1 é aceita"
                    )
                },
                {
                    assertEquals(
                            prioridadeMaxima,
                            comPrioridadeMaxima.prioridade,
                            "prioridade 3 é aceita"
                    )
                },
        )
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT07 - Fluxo de exceção: regra para dispositivo IoT não cadastrado é rejeitada
    @Test
    fun `nao deve cadastrar regra para dispositivo inexistente`() {
        `when`(dispositivoRepository.findById(idDoDispositivo)).thenReturn(Optional.empty())

        val erro =
                assertThrows(NotFoundException::class.java) { service.cadastrar(alertaRequest()) }

        assertEquals("Dispositivo IoT não encontrado", erro.message)
        nenhumaRegraFoiSalva()
    }

    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // CT08 - Fluxo de exceção: regra para espécie não cadastrada é rejeitada
    @Test
    fun `nao deve cadastrar regra para especie inexistente`() {
        dispositivoCadastrado(dispositivo())
        `when`(especieRepository.findById(idDaEspecie)).thenReturn(Optional.empty())

        val erro =
                assertThrows(NotFoundException::class.java) { service.cadastrar(alertaRequest()) }

        assertEquals("Espécie não encontrada", erro.message)
        nenhumaRegraFoiSalva()
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT09 - Fluxo de exceção: regra para dispositivo não autorizado (inativo) é rejeitada
    @Test
    fun `nao deve cadastrar regra para dispositivo inativo`() {
        dispositivoEEspecieCadastrados(dispositivoAtivo = false)

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(alertaRequest())
                }

        assertEquals("O dispositivo IoT está inativo", erro.message)
        nenhumaRegraFoiSalva()
    }

    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // CT10 - Fluxo de exceção: regra para espécie inativa é rejeitada
    @Test
    fun `nao deve cadastrar regra para especie inativa`() {
        dispositivoEEspecieCadastrados(especieAtiva = false)

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(alertaRequest())
                }

        assertEquals("A espécie está inativa", erro.message)
        nenhumaRegraFoiSalva()
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT11 - Fluxo principal: sem filtros, lista todas as regras de monitoramento
    @Test
    fun `deve listar todas as regras quando nao houver filtro`() {
        val regrasCadastradas = listOf(alerta(id = 1), alerta(id = 2))
        `when`(repository.listarTodos()).thenReturn(regrasCadastradas)

        val resultado = service.listar()

        assertEquals(regrasCadastradas.size, resultado.size, "devolve todas as regras cadastradas")
        verify(repository, never()).buscarPorTitulo(anyString() ?: "")
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT12 - Fluxo alternativo: termo em branco é ignorado e termo preenchido usa a busca parcial
    // por título
    @Test
    fun `deve usar busca por titulo somente quando o termo nao estiver em branco`() {
        val termoEmBranco = "   "
        val termoDigitado = " samambaia "
        val termoSemEspacos = "samambaia"
        `when`(repository.listarTodos()).thenReturn(emptyList())
        `when`(repository.buscarPorTitulo(termoSemEspacos)).thenReturn(listOf(alerta()))

        val comTermoEmBranco = service.listar(titulo = termoEmBranco)
        val comTermoDigitado = service.listar(titulo = termoDigitado)

        assertAll(
                "busca por título",
                {
                    assertEquals(
                            0,
                            comTermoEmBranco.size,
                            "termo em branco cai na listagem completa (vazia aqui)"
                    )
                },
                {
                    assertEquals(
                            1,
                            comTermoDigitado.size,
                            "termo preenchido encontra a regra pelo título"
                    )
                },
        )
        verify(repository, times(1)).listarTodos()
        verify(repository, times(1)).buscarPorTitulo(termoSemEspacos)
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT13 - Fluxo alternativo: filtros de status, dispositivo e espécie combinados
    @Test
    fun `deve filtrar regras por status dispositivo e especie`() {
        val dispositivoBuscado = dispositivo(id = idDoDispositivo)
        val outroDispositivo = dispositivo(id = 11, topico = "outro")
        val especieBuscada = especie(id = idDaEspecie)
        val outraEspecie = especie(id = 2)

        val regraProcurada =
                alerta(
                        id = 1,
                        dispositivo = dispositivoBuscado,
                        especie = especieBuscada,
                        ativo = true
                )
        val regraInativa =
                alerta(
                        id = 2,
                        dispositivo = dispositivoBuscado,
                        especie = especieBuscada,
                        ativo = false
                )
        val regraDeOutroDispositivo =
                alerta(id = 3, dispositivo = outroDispositivo, especie = especieBuscada)
        val regraDeOutraEspecie =
                alerta(id = 4, dispositivo = dispositivoBuscado, especie = outraEspecie)
        `when`(repository.listarTodos())
                .thenReturn(
                        listOf(
                                regraProcurada,
                                regraInativa,
                                regraDeOutroDispositivo,
                                regraDeOutraEspecie
                        )
                )

        val resultado =
                service.listar(
                        ativo = true,
                        dispositivoId = idDoDispositivo,
                        especieId = idDaEspecie
                )

        assertEquals(
                listOf(regraProcurada.id),
                resultado.map { it.id },
                "só a regra ativa do dispositivo e da espécie buscados passa pelos três filtros"
        )
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT14 - Fluxo principal: lista só os dispositivos IoT autorizados (ativos)
    @Test
    fun `deve listar apenas dispositivos ativos`() {
        val dispositivoAtivo = dispositivo()
        `when`(dispositivoRepository.findByAtivoTrueOrderByIdAsc())
                .thenReturn(listOf(dispositivoAtivo))

        val resultado = service.listarDispositivos()

        assertEquals(
                listOf(dispositivoAtivo.id),
                resultado.map { it.id },
                "devolve os dispositivos que o banco aponta como ativos"
        )
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT15 - Fluxo principal: consulta uma regra cadastrada pelo id
    @Test
    fun `deve buscar regra por id`() {
        val cadastrada = regraCadastrada(alerta())

        val resultado = service.buscarPorId(idDaRegra)

        assertEquals(cadastrada.id, resultado.id, "devolve a regra do id pedido")
    }

    // RN11: Só é possível consultar ou alterar registros existentes, identificados por id maior que
    // zero.
    // CT16 - Fluxo de exceção: id negativo e regra inexistente são rejeitados
    @Test
    fun `deve rejeitar id invalido e regra inexistente na busca`() {
        val idNegativo = -1L
        val idInexistente = 5L
        `when`(repository.buscarComRelacionamentos(idInexistente)).thenReturn(null)

        val erroIdInvalido =
                assertThrows(BadRequestException::class.java) { service.buscarPorId(idNegativo) }
        val erroInexistente =
                assertThrows(NotFoundException::class.java) { service.buscarPorId(idInexistente) }

        assertAll(
                "consulta de regra inválida",
                {
                    assertEquals(
                            "O ID do alerta deve ser maior que zero",
                            erroIdInvalido.message,
                            "id negativo é rejeitado antes de consultar o banco"
                    )
                },
                {
                    assertEquals(
                            "Alerta não encontrado",
                            erroInexistente.message,
                            "id sem regra cadastrada é rejeitado"
                    )
                },
        )
        verify(repository, never()).buscarComRelacionamentos(idNegativo)
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // RN09: Uma regra de monitoramento deve ter título, prioridade de 1 a 3 e ao menos uma condição
    // monitorada.
    // CT17 - Fluxo alternativo: editar o texto de uma regra cujo dispositivo e espécie ficaram
    // inativos é permitido
    @Test
    fun `deve atualizar regra mantendo dispositivo e especie mesmo que estejam inativos`() {
        val dispositivoInativo = dispositivo(ativo = false)
        val especieInativa = especie(ativo = false)
        regraCadastrada(alerta(dispositivo = dispositivoInativo, especie = especieInativa))
        dispositivoCadastrado(dispositivoInativo)
        especieCadastrada(especieInativa)
        bancoSalvaEGeraId()
        val novoTitulo = "Novo título"
        val novaPrioridade = 3

        val resultado =
                service.atualizar(
                        idDaRegra,
                        alertaRequest(titulo = novoTitulo, prioridade = novaPrioridade)
                )

        assertAll(
                "edição de regra com dispositivo e espécie inativos",
                { assertEquals(novoTitulo, resultado.titulo, "o título é atualizado") },
                { assertEquals(novaPrioridade, resultado.prioridade, "a prioridade é atualizada") },
        )
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT18 - Fluxo de exceção: trocar a regra para um dispositivo inativo não é permitido
    @Test
    fun `nao deve atualizar regra para outro dispositivo inativo`() {
        val outroDispositivoInativo = dispositivo(id = 11, ativo = false)
        regraCadastrada(alerta(dispositivo = dispositivo(id = idDoDispositivo)))
        dispositivoCadastrado(outroDispositivoInativo)
        especieCadastrada(especie())

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.atualizar(
                            idDaRegra,
                            alertaRequest(
                                    dispositivoIotId = requireNotNull(outroDispositivoInativo.id)
                            )
                    )
                }

        assertEquals("O dispositivo IoT está inativo", erro.message)
        nenhumaRegraFoiSalva()
    }

    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // CT19 - Fluxo de exceção: trocar a regra para uma espécie inativa não é permitido
    @Test
    fun `nao deve atualizar regra para outra especie inativa`() {
        val outraEspecieInativa = especie(id = 2, ativo = false)
        regraCadastrada(alerta(especie = especie(id = idDaEspecie)))
        dispositivoCadastrado(dispositivo())
        especieCadastrada(outraEspecieInativa)

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.atualizar(
                            idDaRegra,
                            alertaRequest(especieId = requireNotNull(outraEspecieInativa.id))
                    )
                }

        assertEquals("A espécie está inativa", erro.message)
        nenhumaRegraFoiSalva()
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT20 - Fluxo principal: inativar uma regra é sempre permitido, mesmo com dispositivo inativo
    @Test
    fun `deve inativar regra ativa`() {
        regraCadastrada(alerta(ativo = true, dispositivo = dispositivo(ativo = false)))
        bancoSalvaEGeraId()

        val resultado = service.alternarStatus(idDaRegra)

        assertFalse(resultado.ativo, "a regra ativa fica inativa mesmo com o dispositivo inativo")
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // CT21 - Fluxo alternativo: regra inativa é reativada quando dispositivo e espécie estão ativos
    @Test
    fun `deve reativar regra quando dispositivo e especie estao ativos`() {
        regraCadastrada(alerta(ativo = false))
        bancoSalvaEGeraId()

        val resultado = service.alternarStatus(idDaRegra)

        assertTrue(resultado.ativo, "a regra inativa volta a ficar ativa")
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT22 - Fluxo de exceção: regra cujo dispositivo está inativo não pode ser reativada
    @Test
    fun `nao deve reativar regra com dispositivo inativo`() {
        regraCadastrada(alerta(ativo = false, dispositivo = dispositivo(ativo = false)))

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.alternarStatus(idDaRegra)
                }

        assertEquals("O dispositivo IoT está inativo", erro.message)
        nenhumaRegraFoiSalva()
    }
}
