package tech.pd7.wellbot.data.mqtt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import tech.pd7.wellbot.models.ConnectionStatus

class PahoMqttConnection(private val config: BrokerConfig) : MqttConnection {
    private val client = MqttAsyncClient(config.serverUri, config.clientId, MemoryPersistence())
    private val mutableStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private val mutableMessages = MutableSharedFlow<IncomingMessage>(extraBufferCapacity = 64)

    override val status = mutableStatus.asStateFlow()
    override val messages = mutableMessages.asSharedFlow()

    init {
        client.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                mutableStatus.value = ConnectionStatus.CONNECTED
            }

            override fun connectionLost(cause: Throwable?) {
                mutableStatus.value = ConnectionStatus.RECONNECTING
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                mutableMessages.tryEmit(IncomingMessage(topic, message.payload.toString(Charsets.UTF_8)))
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
        })
    }

    override suspend fun connect() = withContext(Dispatchers.IO) {
        if (client.isConnected) return@withContext
        mutableStatus.value = ConnectionStatus.CONNECTING
        try {
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 20
                mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
                config.username?.let { userName = it }
                config.password?.let { password = it.copyOf() }
            }
            client.connect(options).waitForCompletion(15_000)
            mutableStatus.value = ConnectionStatus.CONNECTED
        } catch (error: Exception) {
            mutableStatus.value = ConnectionStatus.ERROR
            throw error
        }
    }

    override suspend fun subscribe(topic: String, qos: Int) = withContext(Dispatchers.IO) {
        require(qos in 0..2)
        client.subscribe(topic, qos).waitForCompletion(10_000)
    }

    override suspend fun publish(topic: String, payload: String, qos: Int, retained: Boolean) =
        withContext(Dispatchers.IO) {
            check(client.isConnected) { "MQTT desconectado." }
            require(qos in 0..2)
            client.publish(topic, payload.toByteArray(Charsets.UTF_8), qos, retained)
                .waitForCompletion(10_000)
        }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        if (client.isConnected) client.disconnect().waitForCompletion(5_000)
        mutableStatus.value = ConnectionStatus.DISCONNECTED
    }
}
