package org.greenhouse.iot

import jakarta.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

/**
 * Listener MQTT/TCP: conecta no broker configurado em `mqtt.broker-url` (ex.:
 * tcp://broker.hivemq.com:1883), assina os tópicos dos dispositivos IoT e repassa cada mensagem
 * recebida para o callback registrado. Só é criado quando a URL do broker está definida.
 */
@Component
@ConditionalOnExpression("!'\${mqtt.broker-url:}'.isBlank()")
class FilaLeituraIot(
        @Value("\${mqtt.broker-url}") brokerUrl: String,
        @Value("\${mqtt.client-id:greenhouse-server}") clientId: String,
        @Value("\${mqtt.username:}") private val usuario: String,
        @Value("\${mqtt.password:}") private val senha: String,
        @Value("\${mqtt.qos:1}") private val qos: Int,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val assinaturas = ConcurrentHashMap<String, (String) -> Unit>()
    private val client = MqttClient(brokerUrl, clientId, MemoryPersistence())

    init {
        client.setCallback(
                object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String) {
                        log.info("Conectado ao broker MQTT {} (reconexão: {})", serverURI, reconnect)
                        if (reconnect) assinaturas.keys.forEach(::assinarNoBroker)
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
        conectarSeNecessario()
        assinarNoBroker(topico)
    }

    @PreDestroy
    fun encerrar() {
        if (client.isConnected) client.disconnect()
        client.close()
    }

    @Synchronized
    private fun conectarSeNecessario() {
        if (client.isConnected) return
        val opcoes =
                MqttConnectOptions().apply {
                    isAutomaticReconnect = true
                    isCleanSession = true
                    connectionTimeout = 10
                    if (usuario.isNotBlank()) userName = usuario
                    if (senha.isNotBlank()) password = senha.toCharArray()
                }
        client.connect(opcoes)
    }

    private fun assinarNoBroker(topico: String) {
        client.subscribe(topico, qos)
        log.info("Assinado o tópico MQTT {}", topico)
    }
}
