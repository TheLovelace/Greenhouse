package org.greenhouse.integration.fixture

import org.greenhouse.dto.EspecieRequestDTO
import org.greenhouse.dto.EspecieResponseDTO
import org.greenhouse.service.EspecieService
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.util.UUID

/** Cria espécies (e vínculos) temporários e remove tudo em limpar(). */
class EspecieFixture {
    private val jdbc = SpringTestContext.bean<JdbcTemplate>()
    val service = SpringTestContext.bean<EspecieService>()

    private val especies = mutableListOf<Long>()
    private val dispositivos = mutableListOf<Long>()

    fun requisicao(
        nomePopular: String = "Especie teste ${UUID.randomUUID()}",
        nomeCientifico: String = "TST-${UUID.randomUUID()}",
        intervaloRegaDias: Int = 3,
        temperaturaMin: String = "15.00",
        temperaturaMax: String = "28.00",
        umidadeArMin: String? = "40.00",
        umidadeArMax: String? = "70.00",
        umidadeSoloMin: String? = "30.00",
        umidadeSoloMax: String? = "60.00",
    ) = EspecieRequestDTO(
        nomePopular = nomePopular,
        nomeCientifico = nomeCientifico,
        descricao = "Descricao de teste",
        intervaloRegaDias = intervaloRegaDias,
        temperaturaMin = BigDecimal(temperaturaMin),
        temperaturaMax = BigDecimal(temperaturaMax),
        umidadeArMin = umidadeArMin?.let(::BigDecimal),
        umidadeArMax = umidadeArMax?.let(::BigDecimal),
        umidadeSoloMin = umidadeSoloMin?.let(::BigDecimal),
        umidadeSoloMax = umidadeSoloMax?.let(::BigDecimal),
    )

    fun novaEspecie(nomePopular: String = "Especie teste ${UUID.randomUUID()}"): EspecieResponseDTO {
        val criada = service.cadastrar(requisicao(nomePopular = nomePopular))
        especies += criada.id
        return criada
    }

    /** Cria uma regra de alerta (via SQL) que referencia a espécie. */
    fun vincularAlerta(especieId: Long) {
        val sufixo = UUID.randomUUID().toString().take(8)
        val dispositivo = jdbc.queryForObject(
            "INSERT INTO dispositivo_iot (codigo, nome, topico_mqtt) VALUES (?, ?, ?) RETURNING id",
            Long::class.javaObjectType,
            "TST-$sufixo",
            "Dispositivo teste $sufixo",
            "teste/$sufixo",
        )!!
        dispositivos += dispositivo
        jdbc.update(
            "INSERT INTO alerta (dispositivo_iot_id, especie_id, titulo, monitorar_temperatura) VALUES (?, ?, ?, TRUE)",
            dispositivo,
            especieId,
            "Alerta teste $sufixo",
        )
    }

    fun limpar() {
        dispositivos.forEach { id ->
            jdbc.update("DELETE FROM alerta WHERE dispositivo_iot_id = ?", id)
            jdbc.update("DELETE FROM dispositivo_iot WHERE id = ?", id)
        }
        especies.forEach { id ->
            jdbc.update("DELETE FROM alerta WHERE especie_id = ?", id)
            jdbc.update("DELETE FROM produto_especie WHERE especie_id = ?", id)
            jdbc.update("DELETE FROM especie WHERE id = ?", id)
        }
    }
}