package org.greenhouse.integration.especie

import jakarta.validation.Validation
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.EspecieFixture
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestEspecieCrud {

    @Test
    fun `deve cadastrar especie ativa por padrao`() {
        val fixture = EspecieFixture()
        try {
            val criada = fixture.novaEspecie()

            assertTrue(criada.ativo)
            assertTrue(criada.id > 0)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `bean validation deve rejeitar nome em branco, rega invalida e temperatura com digitos demais`() {
        val fixture = EspecieFixture()
        val validator = Validation.buildDefaultValidatorFactory().validator

        val dto = fixture.requisicao(nomePopular = " ", intervaloRegaDias = 0, temperaturaMax = "1000.00")
        val campos = validator.validate(dto).map { it.propertyPath.toString() }.toSet()

        assertTrue("nomePopular" in campos)
        assertTrue("intervaloRegaDias" in campos)
        assertTrue("temperaturaMax" in campos)
    }

    @Test
    fun `deve rejeitar faixa de temperatura invertida`() {
        val fixture = EspecieFixture()
        try {
            val dto = fixture.requisicao(temperaturaMin = "30.00", temperaturaMax = "10.00")

            assertFailsWith<UnprocessableEntityException> { fixture.service.cadastrar(dto) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar faixa de umidade invertida`() {
        val fixture = EspecieFixture()
        try {
            val dto = fixture.requisicao(umidadeSoloMin = "80.00", umidadeSoloMax = "20.00")

            assertFailsWith<UnprocessableEntityException> { fixture.service.cadastrar(dto) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve consultar por id e falhar para inexistente ou invalido`() {
        val fixture = EspecieFixture()
        try {
            val criada = fixture.novaEspecie()

            assertEquals(criada.id, fixture.service.buscarPorId(criada.id).id)
            assertFailsWith<NotFoundException> { fixture.service.buscarPorId(999_999_999L) }
            assertFailsWith<BadRequestException> { fixture.service.buscarPorId(0) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve buscar por nome parcial sem diferenciar maiusculas`() {
        val fixture = EspecieFixture()
        try {
            val token = UUID.randomUUID().toString().take(8).uppercase()
            val criada = fixture.novaEspecie(nomePopular = "Orquidea $token Rara")
            fixture.novaEspecie()

            val encontradas = fixture.service.buscarPorNomeParcial(token.lowercase())

            assertEquals(listOf(criada.id), encontradas.map { it.id })
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve atualizar especie com put e reaplicar validacoes`() {
        val fixture = EspecieFixture()
        try {
            val criada = fixture.novaEspecie()

            val atualizada = fixture.service.atualizar(
                criada.id,
                fixture.requisicao(nomePopular = "Nome novo", intervaloRegaDias = 7),
            )
            assertEquals("Nome novo", atualizada.nomePopular)
            assertEquals(7, atualizada.intervaloRegaDias)

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(criada.id, fixture.requisicao(temperaturaMin = "30.00", temperaturaMax = "10.00"))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve inativar e reativar mantendo a especie consultavel`() {
        val fixture = EspecieFixture()
        try {
            val criada = fixture.novaEspecie()

            assertFalse(fixture.service.alterarStatus(criada.id).ativo)
            assertFalse(fixture.service.buscarPorId(criada.id).ativo)
            assertTrue(fixture.service.alterarStatus(criada.id).ativo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve remover especie sem vinculos`() {
        val fixture = EspecieFixture()
        try {
            val criada = fixture.novaEspecie()

            fixture.service.remover(criada.id)

            assertFailsWith<NotFoundException> { fixture.service.buscarPorId(criada.id) }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `nao deve remover especie com regra de alerta vinculada`() {
        val fixture = EspecieFixture()
        try {
            val criada = fixture.novaEspecie()
            fixture.vincularAlerta(criada.id)

            assertFailsWith<ConflictException> { fixture.service.remover(criada.id) }

            // continua existindo e pode ser inativada
            assertEquals(criada.id, fixture.service.buscarPorId(criada.id).id)
            assertFalse(fixture.service.alterarStatus(criada.id).ativo)
        } finally {
            fixture.limpar()
        }
    }
}