package org.greenhouse.integration.fila

import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.greenhouse.adapter.AppException
import org.greenhouse.adapter.BrokerConnectionException
import org.greenhouse.adapter.GlobalExceptionHandler
import org.greenhouse.adapter.ServerException
import org.greenhouse.iot.FilaLeituraIot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify

/**
 * Testes da fila de leituras IoT contra um broker MQTT. Rodam só pela task `testFila` (ficam fora
 * do `jvmTest` e da pipeline), porque dependem do broker real configurado no `.env.test.local`
 * (MQTT_BROKER_URL, MQTT_USERNAME, MQTT_PASSWORD).
 *
 * Cada teste usa client-id e tópico aleatórios para não derrubar a conexão de um servidor que
 * esteja rodando com o mesmo broker nem receber leituras reais dos dispositivos.
 */
class TestFilaLeituraIot {
    private val usuario = System.getenv("MQTT_USERNAME").orEmpty()
    private val senha = System.getenv("MQTT_PASSWORD").orEmpty()
    private val sufixo = UUID.randomUUID().toString().take(8)

    private fun brokerUrl(): String =
            requireNotNull(System.getenv("MQTT_BROKER_URL")?.takeIf { it.isNotBlank() }) {
                "A variável MQTT_BROKER_URL é necessária para os testes da fila"
            }

    private fun fila(
            url: String,
            exceptionHandler: GlobalExceptionHandler = GlobalExceptionHandler(),
            segundosEntreTentativas: Long = 1,
    ) =
            FilaLeituraIot(
                    exceptionHandler,
                    url,
                    "greenhouse-teste-fila-$sufixo",
                    usuario,
                    senha,
                    1,
                    segundosEntreTentativas,
            )

    private fun publicar(url: String, topico: String, payload: String) {
        val publicador = MqttClient(url, "greenhouse-teste-pub-$sufixo", MemoryPersistence())
        try {
            publicador.connect(
                    MqttConnectOptions().apply {
                        isCleanSession = true
                        connectionTimeout = 10
                        if (usuario.isNotBlank()) userName = usuario
                        if (senha.isNotBlank()) password = senha.toCharArray()
                    },
            )
            publicador.publish(topico, MqttMessage(payload.toByteArray()).apply { qos = 1 })
            publicador.disconnect()
        } finally {
            publicador.close()
        }
    }

    // CT01 - Fluxo principal: mensagem publicada no broker chega ao callback do tópico assinado
    @Test
    fun `deve entregar ao callback a mensagem publicada no topico assinado`() {
        val url = brokerUrl()
        val topico = "greenhouse/teste/$sufixo/sensor"
        val payload = """{"temperatura":25.4,"umidadeAr":60.2,"umidadeSoloRaw":2100}"""
        val recebidas = mutableListOf<String>()
        val chegou = CountDownLatch(1)
        val fila = fila(url)

        try {
            fila.assinar(topico) { mensagem ->
                recebidas += mensagem
                chegou.countDown()
            }
            aguardarAssinatura(fila, topico, url, payload, chegou)

            assertEquals(payload, recebidas.first(), "o callback recebe o payload sem alteração")
        } finally {
            fila.encerrar()
        }
    }

    // CT02 - Fluxo de exceção: broker inacessível não derruba quem assina e a falha é registrada
    // pelo GlobalExceptionHandler, com nova tentativa de conexão
    @Test
    fun `deve registrar falha de conexao no handler sem propagar excecao`() {
        val exceptionHandler = mock(GlobalExceptionHandler::class.java)
        val brokerInacessivel = "tcp://localhost:1"
        val fila = fila(brokerInacessivel, exceptionHandler)

        try {
            assertDoesNotThrow("broker fora do ar não pode derrubar a inicialização") {
                fila.assinar("greenhouse/teste/$sufixo/sensor") {}
            }

            val erros = ArgumentCaptor.forClass(AppException::class.java)
            verify(exceptionHandler, timeout(10_000).atLeast(2))
                    .registrar(erros.capture() ?: ServerException(), anyString() ?: "")
            assertTrue(
                    erros.allValues.all { it is BrokerConnectionException },
                    "cada tentativa sem sucesso é registrada como BrokerConnectionException"
            )
        } finally {
            fila.encerrar()
        }
    }

    /**
     * A assinatura acontece em segundo plano depois da conexão, então a mensagem é publicada de
     * novo até chegar (ou até o tempo limite), em vez de depender de um sleep fixo.
     */
    private fun aguardarAssinatura(
            fila: FilaLeituraIot,
            topico: String,
            url: String,
            payload: String,
            chegou: CountDownLatch,
    ) {
        repeat(15) {
            publicar(url, topico, payload)
            if (chegou.await(2, TimeUnit.SECONDS)) return
        }
        throw AssertionError("A mensagem publicada em $topico não chegou à fila ($fila) em 30s")
    }
}
