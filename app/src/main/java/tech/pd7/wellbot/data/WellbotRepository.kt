package tech.pd7.wellbot.data

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.pd7.wellbot.data.mqtt.BrokerConfig
import tech.pd7.wellbot.data.mqtt.MqttConnection
import tech.pd7.wellbot.data.mqtt.PahoMqttConnection
import tech.pd7.wellbot.data.mqtt.TopicLayout
import tech.pd7.wellbot.data.protocol.WellbotJson
import tech.pd7.wellbot.models.ConnectionStatus
import tech.pd7.wellbot.models.MapTransferRequest
import tech.pd7.wellbot.models.NavigationPoint
import tech.pd7.wellbot.models.NavigationRequest
import tech.pd7.wellbot.models.Robot
import tech.pd7.wellbot.models.RobotMap

interface WellbotRepository {
    val robots: StateFlow<List<Robot>>
    val navigationPoints: StateFlow<List<NavigationPoint>>
    val maps: StateFlow<List<RobotMap>>
    val connectionStatus: StateFlow<ConnectionStatus>
    val isDemo: Boolean

    suspend fun startDiscovery()
    suspend fun openRobotSession(serialNumber: String)
    suspend fun sendNavigationPoint(serialNumber: String, point: NavigationPoint)
    suspend fun transferMap(sourceSerialNumber: String, targetSerialNumber: String, map: RobotMap)
    suspend fun closeRobotSession()
    fun close()
}

class MqttWellbotRepository(
    private val serverUri: String,
    private val topics: TopicLayout = TopicLayout(),
) : WellbotRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableRobots = MutableStateFlow<List<Robot>>(emptyList())
    private val mutableNavigationPoints = MutableStateFlow<List<NavigationPoint>>(emptyList())
    private val mutableMaps = MutableStateFlow<List<RobotMap>>(emptyList())
    private val mutableStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private var discoveryConnection: MqttConnection? = null
    private var robotConnection: MqttConnection? = null

    override val robots = mutableRobots.asStateFlow()
    override val navigationPoints = mutableNavigationPoints.asStateFlow()
    override val maps = mutableMaps.asStateFlow()
    override val connectionStatus = mutableStatus.asStateFlow()
    override val isDemo = false

    override suspend fun startDiscovery() {
        if (discoveryConnection != null) return
        val connection = createConnection("discovery")
        discoveryConnection = connection
        scope.launch { connection.status.collect { mutableStatus.value = it } }
        scope.launch {
            connection.messages.collect { message ->
                if (message.topic == topics.discovery) {
                    runCatching { WellbotJson.decodeRobots(message.payload) }
                        .onSuccess { mutableRobots.value = it.sortedByDescending(Robot::online) }
                }
            }
        }
        connection.connect()
        connection.subscribe(topics.discovery)
    }

    override suspend fun openRobotSession(serialNumber: String) {
        closeRobotSession()
        val connection = createConnection("robot-${serialNumber.take(24)}")
        robotConnection = connection
        scope.launch { connection.status.collect { mutableStatus.value = it } }
        scope.launch {
            connection.messages.collect { message ->
                when (message.topic) {
                    topics.navigationPoints(serialNumber) ->
                        runCatching { WellbotJson.decodeNavigationPoints(message.payload) }
                            .onSuccess { mutableNavigationPoints.value = it }

                    topics.maps(serialNumber) ->
                        runCatching { WellbotJson.decodeMaps(message.payload) }
                            .onSuccess { mutableMaps.value = it }
                }
            }
        }
        connection.connect()
        connection.subscribe(topics.navigationPoints(serialNumber))
        connection.subscribe(topics.maps(serialNumber))
        connection.subscribe(topics.state(serialNumber))
        connection.subscribe(topics.acknowledgements(serialNumber))
    }

    override suspend fun sendNavigationPoint(serialNumber: String, point: NavigationPoint) {
        check(point in mutableNavigationPoints.value) { "Point não disponibilizado para este robô." }
        val request = NavigationRequest(
            requestId = UUID.randomUUID().toString(),
            serialNumber = serialNumber,
            pointId = point.id,
            timestamp = Instant.now().toString(),
        )
        robotConnection?.publish(
            topics.navigationRequests(serialNumber),
            WellbotJson.encodeNavigationRequest(request),
        ) ?: error("Sessão do robô não iniciada.")
    }

    override suspend fun transferMap(
        sourceSerialNumber: String,
        targetSerialNumber: String,
        map: RobotMap,
    ) {
        require(sourceSerialNumber != targetSerialNumber) { "Selecione outro robô como destino." }
        check(map in mutableMaps.value) { "Mapa não pertence ao robô selecionado." }
        check(mutableRobots.value.any { it.serialNumber == targetSerialNumber && it.online }) {
            "O robô de destino precisa estar online."
        }
        val request = MapTransferRequest(
            requestId = UUID.randomUUID().toString(),
            sourceSerialNumber = sourceSerialNumber,
            targetSerialNumber = targetSerialNumber,
            mapId = map.id,
            timestamp = Instant.now().toString(),
        )
        robotConnection?.publish(topics.mapTransfers, WellbotJson.encodeMapTransfer(request))
            ?: error("Sessão do robô não iniciada.")
    }

    override suspend fun closeRobotSession() {
        robotConnection?.disconnect()
        robotConnection = null
        mutableNavigationPoints.value = emptyList()
        mutableMaps.value = emptyList()
    }

    override fun close() {
        scope.cancel()
    }

    private fun createConnection(suffix: String): MqttConnection = PahoMqttConnection(
        BrokerConfig(
            serverUri = serverUri,
            clientId = "wellbot-android-$suffix-${UUID.randomUUID()}",
        ),
    )
}

class DemoWellbotRepository : WellbotRepository {
    private val mutableRobots = MutableStateFlow<List<Robot>>(emptyList())
    private val mutableNavigationPoints = MutableStateFlow<List<NavigationPoint>>(emptyList())
    private val mutableMaps = MutableStateFlow<List<RobotMap>>(emptyList())
    private val mutableStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)

    override val robots = mutableRobots.asStateFlow()
    override val navigationPoints = mutableNavigationPoints.asStateFlow()
    override val maps = mutableMaps.asStateFlow()
    override val connectionStatus = mutableStatus.asStateFlow()
    override val isDemo = true

    override suspend fun startDiscovery() {
        mutableStatus.value = ConnectionStatus.CONNECTED
        mutableRobots.value = listOf(
            Robot("WB-DEMO-001", "WELLBOT Arena", online = true),
            Robot("WB-DEMO-002", "WELLBOT Palco", online = true),
            Robot("WB-DEMO-003", "WELLBOT Recepção", online = false),
            Robot("WB-DEMO-004", "WELLBOT Reserva", online = false),
        )
    }

    override suspend fun openRobotSession(serialNumber: String) {
        mutableNavigationPoints.value = listOf(
            NavigationPoint("entrada", "Entrada principal"),
            NavigationPoint("palco", "Palco"),
            NavigationPoint("credenciamento", "Credenciamento"),
            NavigationPoint("recarga", "Base de recarga"),
        )
        mutableMaps.value = listOf(
            RobotMap("map-evento-a", "Mapa Evento A", "3"),
            RobotMap("map-pavilhao", "Mapa Pavilhão", "1"),
        )
    }

    override suspend fun sendNavigationPoint(serialNumber: String, point: NavigationPoint) = Unit

    override suspend fun transferMap(
        sourceSerialNumber: String,
        targetSerialNumber: String,
        map: RobotMap,
    ) = Unit

    override suspend fun closeRobotSession() {
        mutableNavigationPoints.value = emptyList()
        mutableMaps.value = emptyList()
    }

    override fun close() = Unit
}

object WellbotRepositoryFactory {
    fun create(brokerUri: String): WellbotRepository =
        if (brokerUri.isBlank()) DemoWellbotRepository() else MqttWellbotRepository(brokerUri)
}
