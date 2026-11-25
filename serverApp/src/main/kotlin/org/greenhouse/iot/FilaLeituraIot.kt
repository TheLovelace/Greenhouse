package org.greenhouse.iot

import jakarta.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.greenhouse.adapter.BrokerConnectionException
import org.greenhouse.adapter.GlobalExceptionHandler
import org.greenhouse.adapter.ServerException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

/**
 * Listener MQTT/TCP: conecta no broker configurado em `mqtt.broker-url` (ex.:
 * ssl://<cluster>.hivemq.cloud:8883), assina os tópicos dos dispositivos IoT e repassa cada
 * mensagem recebida para o callback registrado. Só é criado quando a URL do broker está definida.
 *
 */
@Component
@ConditionalOnExpression("!'\${mqtt.broker-url:}'.isBlank()")
class FilaLeituraIot(
        private val exceptionHandler: GlobalExceptionHandler,
        @Value("\${mqtt.broker-url}") brokerUrl: String,
        @Value("\${mqtt.client-id:greenhouse-server}") clientId: String,
        @Value("\${mqtt.username:}") private val usuario: String,
        @Value("\${mqtt.password:}") private val senha: String,
        @Value("\${mqtt.qos:1}") private val qos: Int,
        @Value("\${mqtt.retry-seconds:30}") private val segundosEntreTentativas: Long,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val assinaturas = ConcurrentHashMap<String, (String) -> Unit>()
    private val client = MqttClient(brokerUrl, clientId, MemoryPersistence())
    private val executor =
            Executors.newSingleThreadScheduledExecutor { tarefa ->
                Thread(tarefa, "fila-leitura-iot").apply { isDaemon = true }
            }

    @Volatile private var conectando = false

    init {
        client.setCallback(
                object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String) {
                        log.info("Conectado ao broker MQTT {} (reconexão: {})", serverURI, reconnect)
                        executor.execute(::assinarTodos)
                    }

                    override fun connectionLost(cause: Throwable?) {
                        log.warn("Conexão com o broker MQTT perdida: {}", cause?.message)
                    }

                    override fun messageArrived(topic: String, message: MqttMessage) {
                        val callback = assinaturas[topic] ?: return
                        callback(String(message.payload, Charsets.UTF_8))
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
                },
        )
    }

    fun assinar(topico: String, aoReceber: (payload: String) -> Unit) {
        assinaturas[topico] = aoReceber
        if (client.isConnected) {
            executor.execute { assinarNoBroker(topico) }
        } else {
            agendarConexao(0)
        }
    }

    @PreDestroy
    fun encerrar() {
        executor.shutdownNow()
        try {
            if (client.isConnected) client.disconnect()
            client.close()
        } catch (e: Exception) {
            exceptionHandler.registrar(
                    ServerException("Falha ao encerrar a conexão MQTT", e),
                    CONTEXTO,
            )
        }
    }

    @Synchronized
    private fun agendarConexao(atrasoSegundos: Long) {
        if (conectando || executor.isShutdown) return
        conectando = true
        executor.schedule(::conectar, atrasoSegundos, TimeUnit.SECONDS)
    }

    private fun conectar() {
        try {
            if (!client.isConnected) client.connect(opcoesDeConexao())
            conectando = false
        } catch (e: Exception) {
            exceptionHandler.registrar(
                    BrokerConnectionException(
                            "Não foi possível conectar ao broker MQTT. " +
                                    "Nova tentativa em ${segundosEntreTentativas}s",
                            e,
                    ),
                    CONTEXTO,
            )
            conectando = false
            agendarConexao(segundosEntreTentativas)
        }
    }

    private fun opcoesDeConexao() =
            MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 10
                if (usuario.isNotBlank()) userName = usuario
                if (senha.isNotBlank()) password = senha.toCharArray()
            }

    private fun assinarTodos() = assinaturas.keys.forEach(::assinarNoBroker)

    private fun assinarNoBroker(topico: String) {
        try {
            client.subscribe(topico, qos)
            log.info("Assinado o tópico MQTT {}", topico)
        } catch (e: Exception) {
            exceptionHandler.registrar(
                    BrokerConnectionException("Falha ao assinar o tópico MQTT $topico", e),
                    CONTEXTO,
            )
        }
    }

    private companion object {
        const val CONTEXTO = "Fila IoT"
    }
}
