package org.greenhouse.service

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.AvaliacaoLeituraDTO
import org.greenhouse.dto.LeituraIotPayloadDTO
import org.greenhouse.dto.OcorrenciaAlertaDTO
import org.greenhouse.enums.CondicaoAmbiental
import org.greenhouse.enums.NivelComplexidade
import org.greenhouse.enums.ParametroAmbiental
import org.greenhouse.iot.FilaLeituraIot
import org.greenhouse.model.Alerta
import org.greenhouse.model.DispositivoIot
import org.greenhouse.model.Especie
import org.greenhouse.model.LeituraIot
import org.greenhouse.repository.AlertaRepository
import org.greenhouse.repository.DispositivoIotRepository
import org.greenhouse.repository.LeituraIotRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * RF16 (leitura de dados IoT) e RF17 (monitoramento por IoT): recebe as mensagens da fila, armazena
 * a leitura e a avalia contra a faixa ideal das espécies monitoradas pelo dispositivo.
 */
@Service
class LeituraIotService(
        private val repository: LeituraIotRepository,
        private val dispositivoRepository: DispositivoIotRepository,
        private val alertaRepository: AlertaRepository,
        private val objectMapper: ObjectMapper,
        private val fila: FilaLeituraIot?,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val cem = BigDecimal(100)

    fun iniciarMonitoramento(): Int {
        if (fila == null) {
            log.warn("Fila IoT não configurada, leituras não serão recebidas")
            return 0
        }
        val dispositivos = dispositivoRepository.findByAtivoTrueOrderByIdAsc()
        dispositivos.forEach { dispositivo ->
            fila.assinar(dispositivo.topicoMqtt) { payload ->
                receberComSeguranca(dispositivo.topicoMqtt, payload)
            }
        }
        return dispositivos.size
    }

    @Transactional
    fun processarMensagem(topico: String, payload: String): AvaliacaoLeituraDTO {
        val dispositivo =
                dispositivoRepository.findByTopicoMqtt(topico)
                        ?: throw NotFoundException(
                                "Dispositivo IoT não cadastrado para o tópico informado"
                        )
        if (!dispositivo.ativo) {
            throw UnprocessableEntityException(
                    "O dispositivo IoT não está autorizado a enviar dados"
            )
        }

        val dados = lerPayload(payload)
        validar(dados)

        val leitura =
                repository.save(
                        LeituraIot(
                                dispositivo = dispositivo,
                                temperatura = dados.temperatura,
                                umidadeAr = dados.umidadeAr,
                                umidadeSolo =
                                        converterUmidadeSolo(dispositivo, dados.umidadeSoloRaw),
                                umidadeSoloRaw = dados.umidadeSoloRaw,
                                dataLeitura = dados.dataLeitura ?: OffsetDateTime.now(),
                        ),
                )

        val dispositivoId = requireNotNull(dispositivo.id)
        val ocorrencias =
                alertaRepository
                        .listarAtivosPorDispositivo(dispositivoId)
                        .filter { it.especie.ativo }
                        .flatMap { identificarOcorrencias(it, leitura) }

        return AvaliacaoLeituraDTO(
                leituraId = leitura.id,
                dispositivoId = dispositivoId,
                temperatura = leitura.temperatura,
                umidadeAr = leitura.umidadeAr,
                umidadeSolo = leitura.umidadeSolo,
                dataLeitura = leitura.dataLeitura,
                ocorrencias = ocorrencias,
        )
    }

    fun avaliarCondicoes(
            especie: Especie,
            leitura: LeituraIot
    ): Map<ParametroAmbiental, CondicaoAmbiental> =
            ParametroAmbiental.entries.associateWith {
                classificar(especie, it, valorDe(leitura, it))
            }

    fun faixaDe(especie: Especie, parametro: ParametroAmbiental): Pair<BigDecimal?, BigDecimal?> =
            when (parametro) {
                ParametroAmbiental.TEMPERATURA -> especie.temperaturaMin to especie.temperaturaMax
                ParametroAmbiental.UMIDADE_AR -> especie.umidadeArMin to especie.umidadeArMax
                ParametroAmbiental.UMIDADE_SOLO -> especie.umidadeSoloMin to especie.umidadeSoloMax
            }

    fun classificar(
            especie: Especie,
            parametro: ParametroAmbiental,
            valor: BigDecimal
    ): CondicaoAmbiental {
        val (min, max) = faixaDe(especie, parametro)
        return when {
            min == null && max == null -> CondicaoAmbiental.NAO_CONFIGURADO
            min != null && valor < min -> CondicaoAmbiental.ABAIXO_DO_IDEAL
            max != null && valor > max -> CondicaoAmbiental.ACIMA_DO_IDEAL
            else -> CondicaoAmbiental.IDEAL
        }
    }

    fun nivelComplexidade(especie: Especie): NivelComplexidade {
        val intervalo = especie.intervaloRegaDias
        val pontosRega =
                when {
                    intervalo == null -> 0
                    intervalo <= 2 -> 2
                    intervalo <= 7 -> 1
                    else -> 0
                }
        val pontosTemperatura =
                amplitude(especie, ParametroAmbiental.TEMPERATURA).let { amplitude ->
                    when {
                        amplitude == null -> 0
                        amplitude < BigDecimal("5") -> 2
                        amplitude < BigDecimal("10") -> 1
                        else -> 0
                    }
                }
        val pontosUmidade =
                listOf(ParametroAmbiental.UMIDADE_AR, ParametroAmbiental.UMIDADE_SOLO).count {
                    amplitude(especie, it)?.let { valor -> valor < BigDecimal("20") } ?: false
                }

        return when (pontosRega + pontosTemperatura + pontosUmidade) {
            in 0..1 -> NivelComplexidade.BAIXA
            in 2..3 -> NivelComplexidade.MEDIA
            else -> NivelComplexidade.ALTA
        }
    }

    fun precoCompativel(especie: Especie, preco: BigDecimal): Boolean =
            preco >= nivelComplexidade(especie).precoMinimo

    fun converterUmidadeSolo(dispositivo: DispositivoIot, raw: Int): BigDecimal {
        val seco = dispositivo.calibracaoSeco
        val umido = dispositivo.calibracaoUmido
        if (seco == null || umido == null || seco == umido) {
            throw UnprocessableEntityException(
                    "O dispositivo IoT não possui calibração de umidade do solo válida"
            )
        }

        val percentual =
                BigDecimal(seco - raw)
                        .multiply(cem)
                        .divide(BigDecimal(seco - umido), 2, RoundingMode.HALF_UP)
        return percentual.max(BigDecimal.ZERO.setScale(2)).min(cem.setScale(2))
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): LeituraIot {
        if (id <= 0) throw BadRequestException("O ID da leitura deve ser maior que zero")
        return repository.buscarPorIdComDispositivo(id)
                ?: throw NotFoundException("Leitura IoT não encontrada")
    }

    @Transactional(readOnly = true)
    fun listarDesde(desde: OffsetDateTime, dispositivoId: Long? = null): List<LeituraIot> =
            if (dispositivoId == null) {
                repository.listarDesde(desde)
            } else {
                repository.listarDesdePorDispositivo(dispositivoId, desde)
            }

    private fun receberComSeguranca(topico: String, payload: String) {
        try {
            processarMensagem(topico, payload)
        } catch (e: Exception) {
            log.warn("Leitura IoT descartada no tópico {}: {}", topico, e.message)
        }
    }

    private fun lerPayload(payload: String): LeituraIotPayloadDTO =
            try {
                objectMapper.readValue(payload, LeituraIotPayloadDTO::class.java)
            } catch (e: Exception) {
                throw BadRequestException("Payload da leitura IoT inválido", e)
            }

    private fun validar(dados: LeituraIotPayloadDTO) {
        if (dados.umidadeAr < BigDecimal.ZERO || dados.umidadeAr > BigDecimal(100)) {
            throw UnprocessableEntityException("A umidade do ar deve estar entre 0 e 100")
        }
        if (dados.umidadeSoloRaw < 0) {
            throw UnprocessableEntityException(
                    "A leitura bruta de umidade do solo não pode ser negativa"
            )
        }
    }

    private fun identificarOcorrencias(
            alerta: Alerta,
            leitura: LeituraIot
    ): List<OcorrenciaAlertaDTO> =
            parametrosMonitorados(alerta).mapNotNull { parametro ->
                val valor = valorDe(leitura, parametro)
                val condicao = classificar(alerta.especie, parametro, valor)
                if (!condicao.foraDoLimite) return@mapNotNull null
                OcorrenciaAlertaDTO(
                        alertaId = alerta.id,
                        titulo = alerta.titulo,
                        prioridade = alerta.prioridade,
                        especieId = alerta.especie.id,
                        parametro = parametro,
                        valorMedido = valor,
                        condicao = condicao,
                )
            }

    private fun parametrosMonitorados(alerta: Alerta): List<ParametroAmbiental> = buildList {
        if (alerta.monitorarTemperatura) add(ParametroAmbiental.TEMPERATURA)
        if (alerta.monitorarUmidadeAr) add(ParametroAmbiental.UMIDADE_AR)
        if (alerta.monitorarUmidadeSolo) add(ParametroAmbiental.UMIDADE_SOLO)
    }

    private fun valorDe(leitura: LeituraIot, parametro: ParametroAmbiental): BigDecimal =
            when (parametro) {
                ParametroAmbiental.TEMPERATURA -> leitura.temperatura
                ParametroAmbiental.UMIDADE_AR -> leitura.umidadeAr
                ParametroAmbiental.UMIDADE_SOLO -> leitura.umidadeSolo
            }

    private fun amplitude(especie: Especie, parametro: ParametroAmbiental): BigDecimal? {
        val (min, max) = faixaDe(especie, parametro)
        return if (min != null && max != null) max - min else null
    }
}
