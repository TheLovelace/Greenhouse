package org.greenhouse.integration.fixture

import org.greenhouse.dto.AlertaRequestDTO
import org.greenhouse.dto.AlertaResponseDTO
import org.greenhouse.service.AlertaService
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

/** Cria dispositivos, espécies e regras temporários e remove tudo em limpar(). */
class AlertaFixture {
    private val jdbc = SpringTestContext.bean<JdbcTemplate>()
    val service = SpringTestContext.bean<AlertaService>()

    private val dispositivos = mutableListOf<Long>()
    private val especies = mutableListOf<Long>()

    fun novoDispositivo(): Long {
        val sufixo = UUID.randomUUID().toString().take(8)
        val id = jdbc.queryForObject(
            "INSERT INTO dispositivo_iot (codigo, nome, topico_mqtt, ativo) VALUES (?, ?, ?, TRUE) RETURNING id",
            Long::class.javaObjectType,
            "TST-$sufixo",
            "Dispositivo teste $sufixo",
            "teste/$sufixo",
        )!!
        dispositivos += id
        return id
    }

    fun novaEspecie(ativo: Boolean = true): Long {
        val sufixo = UUID.randomUUID().toString().take(8)
        val id = jdbc.queryForObject(
            """
            INSERT INTO especie (nome_popular, nome_cientifico, temperatura_min, temperatura_max, ativo)
            VALUES (?, ?, 15, 28, ?) RETURNING id
            """.trimIndent(),
            Long::class.javaObjectType,
            "Especie teste $sufixo",
            "TST-$sufixo",
            ativo,
        )!!
        especies += id
        return id
    }

    fun desativarEspecie(id: Long) {
        jdbc.update("UPDATE especie SET ativo = FALSE WHERE id = ?", id)
    }

    fun requisicao(
        dispositivoId: Long,
        especieId: Long,
        titulo: String = "Regra teste ${UUID.randomUUID()}",
        prioridade: Int = 2,
        temperatura: Boolean = true,
        umidadeAr: Boolean = false,
        umidadeSolo: Boolean = false,
    ) = AlertaRequestDTO(
        dispositivoIotId = dispositivoId,
        especieId = especieId,
        titulo = titulo,
        mensagem = "Mensagem de teste",
        prioridade = prioridade,
        monitorarTemperatura = temperatura,
        monitorarUmidadeAr = umidadeAr,
        monitorarUmidadeSolo = umidadeSolo,
    )

    fun novaRegra(
        dispositivoId: Long,
        especieId: Long,
        titulo: String = "Regra teste ${UUID.randomUUID()}",
    ): AlertaResponseDTO = service.cadastrar(requisicao(dispositivoId, especieId, titulo = titulo))

    fun limpar() {
        dispositivos.forEach { id ->
            jdbc.update("DELETE FROM alerta WHERE dispositivo_iot_id = ?", id)
            jdbc.update("DELETE FROM dispositivo_iot WHERE id = ?", id)
        }
        especies.forEach { id ->
            jdbc.update("DELETE FROM alerta WHERE especie_id = ?", id)
            jdbc.update("DELETE FROM especie WHERE id = ?", id)
        }
    }

    companion object {
        /** DTO com título em branco e prioridade 5, para testar o Bean Validation sem acessar o banco. */
        fun dtoInvalido() = AlertaRequestDTO(
            dispositivoIotId = 1L,
            especieId = 1L,
            titulo = " ",
            prioridade = 5,
            monitorarTemperatura = true,
        )
    }
}