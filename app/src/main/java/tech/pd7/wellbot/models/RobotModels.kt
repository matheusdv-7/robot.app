package tech.pd7.wellbot.models

data class Robot(
    val serialNumber: String,
    val displayName: String = serialNumber,
    val online: Boolean = true,
)

data class NavigationPoint(
    val id: String,
    val label: String,
)

data class RobotMap(
    val id: String,
    val name: String,
    val version: String? = null,
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR,
}

data class NavigationRequest(
    val requestId: String,
    val serialNumber: String,
    val pointId: String,
    val timestamp: String,
    val version: Int = 1,
)

data class MapTransferRequest(
    val requestId: String,
    val sourceSerialNumber: String,
    val targetSerialNumber: String,
    val mapId: String,
    val timestamp: String,
    val version: Int = 1,
)
