package tech.pd7.wellbot.data.mqtt

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tech.pd7.wellbot.models.ConnectionStatus

data class IncomingMessage(
    val topic: String,
    val payload: String,
)

data class BrokerConfig(
    val serverUri: String,
    val clientId: String,
    val username: String? = null,
    val password: CharArray? = null,
) {
    init {
        require(serverUri.startsWith("ssl://") || serverUri.startsWith("wss://")) {
            "O broker deve usar TLS (ssl:// ou wss://)."
        }
    }
}

interface MqttConnection {
    val status: StateFlow<ConnectionStatus>
    val messages: SharedFlow<IncomingMessage>

    suspend fun connect()
    suspend fun subscribe(topic: String, qos: Int = 1)
    suspend fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false)
    suspend fun disconnect()
}
