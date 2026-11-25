package org.greenhouse.unit.model

import java.math.BigDecimal
import java.time.OffsetDateTime
import org.greenhouse.model.LeituraIot
import org.greenhouse.unit.fixture.MonitoramentoFixture.dispositivo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE LeituraIot.
 *
 * Não utiliza mocks: o dispositivo de origem vem da fixture
 * [org.greenhouse.unit.fixture.MonitoramentoFixture].
 *
 * Regras de negócio cobertas:
 * - RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
 * disponíveis para consulta.
 */
class LeituraIotTest {
    // RN07: Espécies, cuidados, regras de monitoramento e dados do IoT ficam registrados e
    // disponíveis para consulta.
    // CT01 - Fluxo principal: a leitura registra o dispositivo de origem, as medições e a data
    @Test
    fun `deve registrar o dispositivo as medicoes e a data da leitura`() {
        val leitura = LeituraIot()
        val idNovo = 500L
        val sensorDeOrigem = dispositivo()
        val temperaturaMedida = BigDecimal("23.40")
        val umidadeArMedida = BigDecimal("61.00")
        val umidadeSoloConvertida = BigDecimal("48.50")
        val leituraBrutaDoSolo = 2030
        val momentoDaLeitura = OffsetDateTime.parse("2026-11-10T08:30:00-03:00")

        leitura.id = idNovo
        leitura.dispositivo = sensorDeOrigem
        leitura.temperatura = temperaturaMedida
        leitura.umidadeAr = umidadeArMedida
        leitura.umidadeSolo = umidadeSoloConvertida
        leitura.umidadeSoloRaw = leituraBrutaDoSolo
        leitura.dataLeitura = momentoDaLeitura

        assertAll(
                "dados registrados da leitura",
                { assertEquals(idNovo, leitura.id, "id") },
                { assertSame(sensorDeOrigem, leitura.dispositivo, "dispositivo que enviou a leitura") },
                { assertEquals(temperaturaMedida, leitura.temperatura, "temperatura em °C") },
                { assertEquals(umidadeArMedida, leitura.umidadeAr, "umidade do ar em %") },
                {
                    assertEquals(
                            umidadeSoloConvertida,
                            leitura.umidadeSolo,
                            "umidade do solo convertida em %"
                    )
                },
                {
                    assertEquals(
                            leituraBrutaDoSolo,
                            leitura.umidadeSoloRaw,
                            "leitura bruta do sensor de solo"
                    )
                },
                { assertEquals(momentoDaLeitura, leitura.dataLeitura, "data e hora da leitura") },
        )
    }
}
