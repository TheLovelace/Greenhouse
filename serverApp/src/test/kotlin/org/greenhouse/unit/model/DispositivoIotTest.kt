package org.greenhouse.unit.model

import org.greenhouse.model.DispositivoIot
import org.greenhouse.unit.fixture.MonitoramentoFixture.dispositivo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE DispositivoIot.
 *
 * O dispositivo é criado pela fixture [org.greenhouse.unit.fixture.MonitoramentoFixture] e
 * alterado como o administrador faria ao configurar o monitoramento. Não utiliza mocks.
 *
 * Regras de negócio cobertas:
 * - RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
 * monitorados.
 * - RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %, leitura
 * bruta ≥ 0 e sensor calibrado.
 */
class DispositivoIotTest {
    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT01 - Fluxo principal: dispositivo autorizado é inativado e deixa de poder enviar dados
    @Test
    fun `deve inativar um dispositivo autorizado`() {
        val sensor = dispositivo(ativo = true)

        sensor.ativo = false

        assertFalse(sensor.ativo, "o dispositivo inativado não está mais autorizado")
    }

    // RN08: Parâmetros ambientais e unidades devem estar definidos: umidade entre 0 e 100 %,
    // leitura bruta ≥ 0 e sensor calibrado.
    // CT02 - Fluxo principal: sensor de solo é recalibrado com novos pontos seco e úmido
    @Test
    fun `deve recalibrar o sensor de umidade do solo`() {
        val sensor = dispositivo(calibracaoSeco = 3000, calibracaoUmido = 1000)
        val novoPontoSeco = 3200
        val novoPontoUmido = 1100

        sensor.calibracaoSeco = novoPontoSeco
        sensor.calibracaoUmido = novoPontoUmido

        assertAll(
                "nova calibração do sensor",
                { assertEquals(novoPontoSeco, sensor.calibracaoSeco, "leitura de solo seco (0 %)") },
                {
                    assertEquals(
                            novoPontoUmido,
                            sensor.calibracaoUmido,
                            "leitura de solo encharcado (100 %)"
                    )
                },
        )
    }

    // RN05: Somente dispositivos IoT cadastrados e autorizados (ativos) podem enviar dados e ser
    // monitorados.
    // CT03 - Fluxo alternativo: identificação, local e tópico MQTT do dispositivo são atualizados
    @Test
    fun `deve atualizar a identificacao e o topico do dispositivo`() {
        val sensor = DispositivoIot()
        val idNovo = 20L
        val codigoNovo = "ESP32-020"
        val nomeNovo = "Sensor estufa 2"
        val localizacaoNova = "Estufa 2"
        val topicoNovo = "greenhouse/estufa-2/leituras"

        sensor.id = idNovo
        sensor.codigo = codigoNovo
        sensor.nome = nomeNovo
        sensor.localizacao = localizacaoNova
        sensor.topicoMqtt = topicoNovo

        assertAll(
                "identificação do dispositivo",
                { assertEquals(idNovo, sensor.id, "id") },
                { assertEquals(codigoNovo, sensor.codigo, "código") },
                { assertEquals(nomeNovo, sensor.nome, "nome") },
                { assertEquals(localizacaoNova, sensor.localizacao, "localização") },
                { assertEquals(topicoNovo, sensor.topicoMqtt, "tópico MQTT de onde vêm as leituras") },
        )
    }
}
