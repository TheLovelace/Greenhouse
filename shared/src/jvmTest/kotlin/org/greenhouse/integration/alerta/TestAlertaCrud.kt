package org.greenhouse.integration.alerta

import jakarta.validation.Validation
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.AlertaFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestAlertaCrud {

    @Test
    fun `deve cadastrar regra valida ativa por padrao`() {
        val fixture = AlertaFixture()
        try {
            val criada = fixture.novaRegra(fixture.novoDispositivo(), fixture.novaEspecie())

            assertTrue(criada.ativo)
            assertTrue(criada.id > 0)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar regra sem nenhuma condicao monitorada`() {
        val fixture = AlertaFixture()
        try {
            val dto = fixture.requisicao(fixture.novoDispositivo(), fixture.novaEspecie(), temperatura = false)

            assertFailsWith<UnprocessableEntityException> { fixture.service.cadastrar(dto) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar dispositivo ou especie inexistente`() {
        val fixture = AlertaFixture()
        try {
            val dispositivo = fixture.novoDispositivo()
            val especie = fixture.novaEspecie()

            assertFailsWith<NotFoundException> { fixture.service.cadastrar(fixture.requisicao(999_999_999L, especie)) }
            assertFailsWith<NotFoundException> { fixture.service.cadastrar(fixture.requisicao(dispositivo, 999_999_999L)) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar especie inativa no cadastro`() {
        val fixture = AlertaFixture()
        try {
            val dto = fixture.requisicao(fixture.novoDispositivo(), fixture.novaEspecie(ativo = false))

            assertFailsWith<UnprocessableEntityException> { fixture.service.cadastrar(dto) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `bean validation deve rejeitar titulo em branco e prioridade fora de 1 a 3`() {
        val validator = Validation.buildDefaultValidatorFactory().validator
        val dto = AlertaFixture.dtoInvalido()

        val campos = validator.validate(dto).map { it.propertyPath.toString() }.toSet()

        assertTrue("titulo" in campos)
        assertTrue("prioridade" in campos)
    }

    @Test
    fun `deve consultar por id e falhar para inexistente ou invalido`() {
        val fixture = AlertaFixture()
        try {
            val criada = fixture.novaRegra(fixture.novoDispositivo(), fixture.novaEspecie())

            assertEquals(criada.id, fixture.service.buscarPorId(criada.id).id)
            assertFailsWith<NotFoundException> { fixture.service.buscarPorId(999_999_999L) }
            assertFailsWith<BadRequestException> { fixture.service.buscarPorId(0) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve listar com busca parcial por titulo sem diferenciar maiusculas`() {
        val fixture = AlertaFixture()
        try {
            val dispositivo = fixture.novoDispositivo()
            val especie = fixture.novaEspecie()
            val criada = fixture.novaRegra(dispositivo, especie, titulo = "Temperatura Estufa XYZ123")
            fixture.novaRegra(dispositivo, especie, titulo = "Outro alerta")

            val encontrados = fixture.service.listar(titulo = "estufa xyz")

            assertEquals(listOf(criada.id), encontrados.map { it.id })
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve atualizar regra com put`() {
        val fixture = AlertaFixture()
        try {
            val dispositivo = fixture.novoDispositivo()
            val especie = fixture.novaEspecie()
            val criada = fixture.novaRegra(dispositivo, especie)

            val atualizada = fixture.service.atualizar(
                criada.id,
                fixture.requisicao(dispositivo, especie, titulo = "Titulo novo", prioridade = 1, temperatura = false, umidadeSolo = true),
            )

            assertEquals("Titulo novo", atualizada.titulo)
            assertEquals(1, atualizada.prioridade)
            assertFalse(atualizada.monitorarTemperatura)
            assertTrue(atualizada.monitorarUmidadeSolo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve inativar e reativar preservando o registro`() {
        val fixture = AlertaFixture()
        try {
            val criada = fixture.novaRegra(fixture.novoDispositivo(), fixture.novaEspecie())

            assertFalse(fixture.service.alternarStatus(criada.id).ativo)
            assertFalse(fixture.service.buscarPorId(criada.id).ativo)
            assertTrue(fixture.service.alternarStatus(criada.id).ativo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `nao deve reativar regra com especie inativa mas permite editar titulo`() {
        val fixture = AlertaFixture()
        try {
            val dispositivo = fixture.novoDispositivo()
            val especie = fixture.novaEspecie()
            val criada = fixture.novaRegra(dispositivo, especie)
            fixture.service.alternarStatus(criada.id)
            fixture.desativarEspecie(especie)

            assertFailsWith<UnprocessableEntityException> { fixture.service.alternarStatus(criada.id) }

            val editada = fixture.service.atualizar(criada.id, fixture.requisicao(dispositivo, especie, titulo = "Editado"))
            assertEquals("Editado", editada.titulo)
        } finally {
            fixture.limpar()
        }
    }
}