package org.greenhouse.unit.fixture

import java.math.BigDecimal
import org.greenhouse.dto.AlertaRequestDTO
import org.greenhouse.dto.EspecieRequestDTO
import org.greenhouse.model.Alerta
import org.greenhouse.model.DispositivoIot
import org.greenhouse.model.Especie

/**
 * FIXTURE (test data builder) do caso de uso "Configurar cuidados e monitoramento de plantas".
 * Valores padrão:
 * - especie(): faixa ideal 18–26 °C, ar 50–80 %, solo 40–70 %, rega a cada 3 dias.
 * - dispositivo(): calibração do sensor de solo 3000 (seco) = 0 % e 1000 (encharcado) = 100 %.
 * - payload(): JSON no formato publicado pelo dispositivo no broker MQTT.
 */
object MonitoramentoFixture {
        fun especie(
                id: Long? = 1L,
                ativo: Boolean = true,
                intervaloRegaDias: Int? = 3,
                temperaturaMin: BigDecimal? = BigDecimal("18.00"),
                temperaturaMax: BigDecimal? = BigDecimal("26.00"),
                umidadeArMin: BigDecimal? = BigDecimal("50.00"),
                umidadeArMax: BigDecimal? = BigDecimal("80.00"),
                umidadeSoloMin: BigDecimal? = BigDecimal("40.00"),
                umidadeSoloMax: BigDecimal? = BigDecimal("70.00"),
        ) =
                Especie(
                        id = id,
                        nomePopular = "Samambaia",
                        nomeCientifico = "Nephrolepis exaltata",
                        descricao = "Prefere meia-sombra",
                        intervaloRegaDias = intervaloRegaDias,
                        temperaturaMin = temperaturaMin,
                        temperaturaMax = temperaturaMax,
                        umidadeArMin = umidadeArMin,
                        umidadeArMax = umidadeArMax,
                        umidadeSoloMin = umidadeSoloMin,
                        umidadeSoloMax = umidadeSoloMax,
                        ativo = ativo,
                )

        fun especieRequest(
                temperaturaMin: BigDecimal = BigDecimal("18.00"),
                temperaturaMax: BigDecimal = BigDecimal("26.00"),
                umidadeArMin: BigDecimal? = BigDecimal("50.00"),
                umidadeArMax: BigDecimal? = BigDecimal("80.00"),
                umidadeSoloMin: BigDecimal? = BigDecimal("40.00"),
                umidadeSoloMax: BigDecimal? = BigDecimal("70.00"),
        ) =
                EspecieRequestDTO(
                        nomePopular = "  Samambaia  ",
                        nomeCientifico = "Nephrolepis exaltata",
                        descricao = "Prefere meia-sombra",
                        intervaloRegaDias = 3,
                        temperaturaMin = temperaturaMin,
                        temperaturaMax = temperaturaMax,
                        umidadeArMin = umidadeArMin,
                        umidadeArMax = umidadeArMax,
                        umidadeSoloMin = umidadeSoloMin,
                        umidadeSoloMax = umidadeSoloMax,
                )

        fun dispositivo(
                id: Long? = 10L,
                ativo: Boolean = true,
                topico: String = "greenhouse/estufa-1/leituras",
                calibracaoSeco: Int? = 3000,
                calibracaoUmido: Int? = 1000,
        ) =
                DispositivoIot(
                        id = id,
                        codigo = "ESP32-0$id",
                        nome = "Sensor estufa $id",
                        localizacao = "Estufa 1",
                        topicoMqtt = topico,
                        calibracaoSeco = calibracaoSeco,
                        calibracaoUmido = calibracaoUmido,
                        ativo = ativo,
                )

        fun alerta(
                id: Long? = 100L,
                dispositivo: DispositivoIot = dispositivo(),
                especie: Especie = especie(),
                ativo: Boolean = true,
                monitorarTemperatura: Boolean = true,
                monitorarUmidadeAr: Boolean = true,
                monitorarUmidadeSolo: Boolean = true,
                prioridade: Int = 2,
        ) =
                Alerta(
                        id = id,
                        dispositivo = dispositivo,
                        especie = especie,
                        titulo = "Condições da samambaia",
                        mensagem = "Verificar estufa",
                        monitorarTemperatura = monitorarTemperatura,
                        monitorarUmidadeAr = monitorarUmidadeAr,
                        monitorarUmidadeSolo = monitorarUmidadeSolo,
                        ativo = ativo,
                        prioridade = prioridade,
                )

        fun alertaRequest(
                dispositivoIotId: Long = 10L,
                especieId: Long = 1L,
                titulo: String = "  Condições da samambaia  ",
                prioridade: Int = 2,
                monitorarTemperatura: Boolean = true,
                monitorarUmidadeAr: Boolean = false,
                monitorarUmidadeSolo: Boolean = false,
        ) =
                AlertaRequestDTO(
                        dispositivoIotId = dispositivoIotId,
                        especieId = especieId,
                        titulo = titulo,
                        mensagem = "Verificar estufa",
                        prioridade = prioridade,
                        monitorarTemperatura = monitorarTemperatura,
                        monitorarUmidadeAr = monitorarUmidadeAr,
                        monitorarUmidadeSolo = monitorarUmidadeSolo,
                )

        fun payload(
                temperatura: String = "22.50",
                umidadeAr: String = "65.00",
                umidadeSoloRaw: Int = 2000,
                dataLeitura: String? = null,
        ): String {
                val data = dataLeitura?.let { ""","dataLeitura":"$it"""" } ?: ""
                return """{"temperatura":$temperatura,"umidadeAr":$umidadeAr,"umidadeSoloRaw":$umidadeSoloRaw$data}"""
        }
}
