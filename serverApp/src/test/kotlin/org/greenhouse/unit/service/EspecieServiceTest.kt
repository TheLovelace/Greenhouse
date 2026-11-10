package org.greenhouse.unit.service

import java.math.BigDecimal
import java.util.Optional
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.mapper.EspecieMapper
import org.greenhouse.model.Especie
import org.greenhouse.repository.EspecieRepository
import org.greenhouse.service.EspecieService
import org.greenhouse.unit.fixture.MonitoramentoFixture.especie
import org.greenhouse.unit.fixture.MonitoramentoFixture.especieRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

/**
 * Testes de unidade do service EspecieService (RF15 - cuidados e espécies de plantas).
 *
 * Recursos de teste usados:
 * - MOCK [repository]: substitui o acesso ao banco (JPA).
 * - SPY [mapper]: o EspecieMapper real é usado mas embrulhado em spy para o @InjectMocks injetá-lo
 * no service.
 * - FIXTURE: objetos válidos reais vindos de MonitoramentoFixture.
 * - DRIVER: cada método de teste age como driver, chamando o service diretamente.
 *
 * Regras de negócio cobertas:
 * - RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
 * umidades), com mínimo ≤ máximo.
 * - RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
 * - RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
 * disponíveis para consulta.
 * - RN10: Espécie com histórico (regras de alerta ou produtos vinculados) não pode ser removida,
 * apenas inativada.
 * - RN11: Só é possível consultar ou alterar registros existentes, identificados por id maior que
 * zero.
 */
@ExtendWith(MockitoExtension::class)
class EspecieServiceTest {
    @Mock lateinit var repository: EspecieRepository

    @Spy var mapper = EspecieMapper()

    @InjectMocks lateinit var service: EspecieService

    private val idDaEspecie = 1L
    private val idInexistente = 99L

    private fun bancoSalvaEGeraId(id: Long = idDaEspecie) {
        `when`(repository.save(any(Especie::class.java))).thenAnswer { invocacao ->
            invocacao.getArgument<Especie>(0).also { it.id = id }
        }
    }

    private fun especieCadastrada(especie: Especie = especie()): Especie {
        `when`(repository.findById(idDaEspecie)).thenReturn(Optional.of(especie))
        return especie
    }

    private fun vinculosDaEspecie(alertas: Long, produtos: Long? = null) {
        `when`(repository.contarAlertas(idDaEspecie)).thenReturn(alertas)
        produtos?.let { `when`(repository.contarProdutosVinculados(idDaEspecie)).thenReturn(it) }
    }

    private fun nenhumaEspecieFoiSalva() =
            verify(repository, never()).save(any(Especie::class.java))

    private fun nenhumaEspecieFoiRemovida() =
            verify(repository, never()).delete(any(Especie::class.java))

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT01 - Fluxo principal: espécie é cadastrada com suas orientações de cuidado
    @Test
    fun `deve cadastrar especie com cuidados recomendados`() {
        bancoSalvaEGeraId()
        val cuidadosInformados = especieRequest()
        val especieEnviadaAoBanco = ArgumentCaptor.forClass(Especie::class.java)

        val resultado = service.cadastrar(cuidadosInformados)

        verify(repository, times(1)).save(especieEnviadaAoBanco.capture())
        val salva = especieEnviadaAoBanco.value
        assertAll(
                "espécie cadastrada com seus cuidados",
                {
                    assertEquals(
                            "Samambaia",
                            salva.nomePopular,
                            "o nome é salvo sem espaços nas pontas"
                    )
                },
                {
                    assertEquals(
                            cuidadosInformados.intervaloRegaDias,
                            salva.intervaloRegaDias,
                            "salva a frequência de rega"
                    )
                },
                {
                    assertEquals(
                            cuidadosInformados.temperaturaMin,
                            salva.temperaturaMin,
                            "salva a temperatura mínima"
                    )
                },
                {
                    assertEquals(
                            cuidadosInformados.umidadeSoloMax,
                            salva.umidadeSoloMax,
                            "salva a umidade do solo máxima"
                    )
                },
                { assertEquals(idDaEspecie, resultado.id, "devolve o id gerado pelo banco") },
                { assertTrue(resultado.ativo, "toda espécie nova começa ativa") },
        )
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT02 - Fluxo de exceção: temperatura mínima maior que a máxima é rejeitada antes de chegar ao
    // banco
    @Test
    fun `nao deve cadastrar especie com temperatura minima maior que a maxima`() {
        val faixaInvertida =
                especieRequest(temperaturaMin = BigDecimal("30"), temperaturaMax = BigDecimal("20"))

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(faixaInvertida)
                }

        assertEquals("A temperatura mínima não pode ser maior que a máxima", erro.message)
        nenhumaEspecieFoiSalva()
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT03 - Fluxo de exceção: umidade do ar mínima maior que a máxima é rejeitada
    @Test
    fun `nao deve cadastrar especie com umidade do ar minima maior que a maxima`() {
        val faixaInvertida =
                especieRequest(umidadeArMin = BigDecimal("90"), umidadeArMax = BigDecimal("40"))

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(faixaInvertida)
                }

        assertEquals("A umidade do ar mínima não pode ser maior que a máxima", erro.message)
        nenhumaEspecieFoiSalva()
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT04 - Fluxo de exceção: umidade do solo mínima maior que a máxima é rejeitada
    @Test
    fun `nao deve cadastrar especie com umidade do solo minima maior que a maxima`() {
        val faixaInvertida =
                especieRequest(umidadeSoloMin = BigDecimal("80"), umidadeSoloMax = BigDecimal("10"))

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.cadastrar(faixaInvertida)
                }

        assertEquals("A umidade do solo mínima não pode ser maior que a máxima", erro.message)
        nenhumaEspecieFoiSalva()
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT05 - Análise de valor limite: mínimo igual ao máximo é uma faixa válida
    @Test
    fun `deve cadastrar especie quando minimo for igual ao maximo`() {
        bancoSalvaEGeraId()
        val temperaturaExata = BigDecimal("22")
        val faixaDeUmValorSo =
                especieRequest(temperaturaMin = temperaturaExata, temperaturaMax = temperaturaExata)

        val resultado = service.cadastrar(faixaDeUmValorSo)

        assertAll(
                "faixa com mínimo igual ao máximo",
                { assertEquals(temperaturaExata, resultado.temperaturaMin, "aceita o mínimo") },
                {
                    assertEquals(
                            temperaturaExata,
                            resultado.temperaturaMax,
                            "aceita o máximo igual ao mínimo"
                    )
                },
        )
        verify(repository, times(1)).save(any(Especie::class.java))
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT06 - Fluxo alternativo: umidades são opcionais: faixas ausentes não são validadas
    @Test
    fun `deve cadastrar especie sem faixas de umidade configuradas`() {
        bancoSalvaEGeraId()
        val semUmidadeDoArESemMaximoDoSolo =
                especieRequest(
                        umidadeArMin = null,
                        umidadeArMax = null,
                        umidadeSoloMin = BigDecimal("40"),
                        umidadeSoloMax = null
                )

        val resultado = service.cadastrar(semUmidadeDoArESemMaximoDoSolo)

        assertAll(
                "umidades opcionais",
                { assertNull(resultado.umidadeArMin, "a umidade do ar pode ficar sem faixa") },
                { assertNull(resultado.umidadeSoloMax, "o solo pode ter só o mínimo") },
        )
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT07 - Fluxo principal: lista todas as espécies cadastradas
    @Test
    fun `deve listar todas as especies`() {
        val especiesCadastradas = listOf(especie(id = 1), especie(id = 2))
        `when`(repository.findAll()).thenReturn(especiesCadastradas)

        val resultado = service.listar()

        assertEquals(
                especiesCadastradas.map { it.id },
                resultado.map { it.id },
                "devolve todas as espécies cadastradas"
        )
        verify(repository, times(1)).findAll()
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT08 - Fluxo principal: consulta uma espécie cadastrada pelo id
    @Test
    fun `deve buscar especie por id`() {
        val cadastrada = especieCadastrada()

        val resultado = service.buscarPorId(idDaEspecie)

        assertEquals(
                cadastrada.nomePopular,
                resultado.nomePopular,
                "devolve a espécie do id pedido"
        )
        verify(repository, times(1)).findById(idDaEspecie)
    }

    // RN11: Só é possível consultar ou alterar registros existentes, identificados por id maior que
    // zero.
    // CT09 - Análise de valor limite: id 0 é inválido e nem chega a consultar o banco
    @Test
    fun `nao deve buscar especie com id menor ou igual a zero`() {
        val idInvalido = 0L

        val erro = assertThrows(BadRequestException::class.java) { service.buscarPorId(idInvalido) }

        assertEquals("O id da espécie deve ser maior que zero", erro.message)
        verify(repository, never()).findById(anyLong())
    }

    // RN11: Só é possível consultar ou alterar registros existentes, identificados por id maior que
    // zero.
    // CT10 - Fluxo de exceção: consulta de espécie inexistente é rejeitada
    @Test
    fun `deve lancar excecao ao buscar especie inexistente`() {
        `when`(repository.findById(idInexistente)).thenReturn(Optional.empty())

        val erro =
                assertThrows(NotFoundException::class.java) { service.buscarPorId(idInexistente) }

        assertEquals("Espécie não encontrada", erro.message)
    }

    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT11 - Fluxo alternativo: busca por nome parcial ignora espaços nas pontas do termo
    @Test
    fun `deve buscar especies por nome parcial sem espacos nas pontas`() {
        val termoDigitado = "  sama "
        val termoSemEspacos = "sama"
        `when`(repository.buscarPorNomePopular(termoSemEspacos)).thenReturn(listOf(especie()))

        val resultado = service.buscarPorNomeParcial(termoDigitado)

        assertEquals(1, resultado.size, "encontra a samambaia pelo trecho do nome")
        verify(repository, times(1)).buscarPorNomePopular(termoSemEspacos)
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT12 - Fluxo principal: atualiza os cuidados de uma espécie existente
    @Test
    fun `deve atualizar os cuidados de uma especie existente`() {
        val existente = especieCadastrada(especie(intervaloRegaDias = 7))
        `when`(repository.save(existente)).thenReturn(existente)
        val novosCuidados =
                especieRequest(temperaturaMin = BigDecimal("15"), temperaturaMax = BigDecimal("28"))

        val resultado = service.atualizar(idDaEspecie, novosCuidados)

        assertAll(
                "cuidados atualizados",
                {
                    assertEquals(
                            novosCuidados.intervaloRegaDias,
                            resultado.intervaloRegaDias,
                            "a rega passa de 7 para 3 dias"
                    )
                },
                {
                    assertEquals(
                            novosCuidados.temperaturaMin,
                            resultado.temperaturaMin,
                            "nova temperatura mínima"
                    )
                },
                {
                    assertEquals(
                            novosCuidados.temperaturaMax,
                            resultado.temperaturaMax,
                            "nova temperatura máxima"
                    )
                },
        )
        verify(repository, times(1)).save(existente)
    }

    // RN01: Cada espécie deve possuir orientações de cuidado específicas (rega, temperatura e
    // umidades), com mínimo ≤ máximo.
    // CT13 - Fluxo de exceção: atualização com faixa inválida não consulta nem salva
    @Test
    fun `nao deve atualizar especie com faixa invalida`() {
        val faixaInvertida =
                especieRequest(temperaturaMin = BigDecimal("40"), temperaturaMax = BigDecimal("10"))

        assertThrows(
                UnprocessableEntityException::class.java,
                { service.atualizar(idDaEspecie, faixaInvertida) },
                "faixa com mínimo maior que o máximo deve ser rejeitada"
        )

        verify(repository, never()).findById(anyLong())
        nenhumaEspecieFoiSalva()
    }

    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // RN10: Espécie com histórico (regras de alerta ou produtos vinculados) não pode ser removida,
    // apenas inativada.
    // CT14 - Fluxo principal: espécie ativa passa a inativa e deixa de ser monitorada
    @Test
    fun `deve inativar especie ativa ao alterar status`() {
        val ativa = especieCadastrada(especie(ativo = true))
        `when`(repository.save(ativa)).thenReturn(ativa)

        val resultado = service.alterarStatus(idDaEspecie)

        assertFalse(resultado.ativo, "a espécie ativa deve ficar inativa")
    }

    // RN06: Somente espécies cadastradas e ativas podem ser monitoradas.
    // CT15 - Fluxo alternativo: espécie inativa volta a ficar ativa
    @Test
    fun `deve reativar especie inativa ao alterar status`() {
        val inativa = especieCadastrada(especie(ativo = false))
        `when`(repository.save(inativa)).thenReturn(inativa)

        val resultado = service.alterarStatus(idDaEspecie)

        assertTrue(resultado.ativo, "a espécie inativa deve voltar a ficar ativa")
    }

    // RN10: Espécie com histórico (regras de alerta ou produtos vinculados) não pode ser removida,
    // apenas inativada.
    // CT16 - Fluxo principal: espécie sem vínculos é removida
    @Test
    fun `deve remover especie sem alertas nem produtos vinculados`() {
        val semVinculos = especieCadastrada()
        vinculosDaEspecie(alertas = 0, produtos = 0)

        service.remover(idDaEspecie)

        verify(repository, times(1)).delete(semVinculos)
    }

    // RN10: Espécie com histórico (regras de alerta ou produtos vinculados) não pode ser removida,
    // apenas inativada.
    // CT17 - Fluxo de exceção: espécie com regras de alerta não pode ser apagada
    @Test
    fun `nao deve remover especie com alertas vinculados`() {
        especieCadastrada()
        vinculosDaEspecie(alertas = 2)

        val erro = assertThrows(ConflictException::class.java) { service.remover(idDaEspecie) }

        assertEquals(
                "A espécie possui regras de alerta vinculadas; inative-a em vez de removê-la",
                erro.message
        )
        nenhumaEspecieFoiRemovida()
    }

    // RN10: Espécie com histórico (regras de alerta ou produtos vinculados) não pode ser removida,
    // apenas inativada.
    // CT18 - Fluxo de exceção: espécie vinculada a produtos não pode ser apagada
    @Test
    fun `nao deve remover especie vinculada a produtos`() {
        especieCadastrada()
        vinculosDaEspecie(alertas = 0, produtos = 1)

        val erro = assertThrows(ConflictException::class.java) { service.remover(idDaEspecie) }

        assertEquals(
                "A espécie está vinculada a produtos; inative-a em vez de removê-la",
                erro.message
        )
        nenhumaEspecieFoiRemovida()
    }
}
