package tech.pd7.wellbot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.pd7.wellbot.auth.AuthGateway
import tech.pd7.wellbot.data.WellbotRepository
import tech.pd7.wellbot.models.ConnectionStatus
import tech.pd7.wellbot.models.NavigationPoint
import tech.pd7.wellbot.models.Robot
import tech.pd7.wellbot.models.RobotMap

data class WellbotUiState(
    val isLoggedIn: Boolean = false,
    val robots: List<Robot> = emptyList(),
    val selectedRobot: Robot? = null,
    val navigationPoints: List<NavigationPoint> = emptyList(),
    val maps: List<RobotMap> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isDemo: Boolean = false,
    val isBusy: Boolean = false,
    val showMapSwitch: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val lastPoint: NavigationPoint? = null,
)

class WellbotViewModel(
    private val repository: WellbotRepository,
    private val authGateway: AuthGateway,
) : ViewModel() {
    private data class SessionState(
        val isLoggedIn: Boolean,
        val selectedRobot: Robot?,
        val isBusy: Boolean,
        val showMapSwitch: Boolean,
    )

    private data class FeedbackState(
        val errorMessage: String?,
        val infoMessage: String?,
        val lastPoint: NavigationPoint?,
    )

    private val isLoggedIn = MutableStateFlow(false)
    private val selectedRobot = MutableStateFlow<Robot?>(null)
    private val isBusy = MutableStateFlow(false)
    private val showMapSwitch = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val infoMessage = MutableStateFlow<String?>(null)
    private val lastPoint = MutableStateFlow<NavigationPoint?>(null)

    private val sessionState = combine(isLoggedIn, selectedRobot, isBusy, showMapSwitch) {
            loggedIn,
            robot,
            busy,
            mapSwitch,
        ->
        SessionState(loggedIn, robot, busy, mapSwitch)
    }

    private val feedbackState = combine(errorMessage, infoMessage, lastPoint) { error, info, point ->
        FeedbackState(error, info, point)
    }

    private val localState = combine(sessionState, feedbackState) { session, feedback ->
        session to feedback
    }

    val uiState: StateFlow<WellbotUiState> = combine(
        repository.robots,
        repository.navigationPoints,
        repository.maps,
        repository.connectionStatus,
        localState,
    ) { robots, points, maps, status, local ->
        val (session, feedback) = local
        WellbotUiState(
            isLoggedIn = session.isLoggedIn,
            robots = robots.sortedWith(compareByDescending<Robot> { it.online }.thenBy { it.displayName }),
            selectedRobot = session.selectedRobot,
            navigationPoints = points,
            maps = maps,
            connectionStatus = status,
            isDemo = repository.isDemo,
            isBusy = session.isBusy,
            showMapSwitch = session.showMapSwitch,
            errorMessage = feedback.errorMessage,
            infoMessage = feedback.infoMessage,
            lastPoint = feedback.lastPoint,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        WellbotUiState(isDemo = repository.isDemo),
    )

    /**
     * O DemoAuthGateway libera esta etapa sem validar senha. Quando a nuvem estiver
     * pronta, basta injetar CloudAuthGateway sem alterar a tela de login.
     */
    fun login(username: String, password: String) = runAction {
        authGateway.login(username, password)
        isLoggedIn.value = true
        repository.startDiscovery()
    }

    fun selectRobot(robot: Robot) {
        if (!robot.online) return
        runAction {
            repository.openRobotSession(robot.serialNumber)
            selectedRobot.value = robot
        }
    }

    fun send(point: NavigationPoint) {
        val robot = selectedRobot.value ?: return
        runAction {
            repository.sendNavigationPoint(robot.serialNumber, point)
            lastPoint.value = point
            infoMessage.value = "Navegação enviada para ${point.label}."
        }
    }

    fun openMapSwitch() { showMapSwitch.value = true }
    fun closeMapSwitch() { showMapSwitch.value = false }

    fun transferMap(map: RobotMap, targetRobot: Robot) {
        val sourceRobot = selectedRobot.value ?: return
        runAction {
            repository.transferMap(sourceRobot.serialNumber, targetRobot.serialNumber, map)
            showMapSwitch.value = false
            infoMessage.value = if (repository.isDemo) {
                "Demonstração: ${map.name} preparado para ${targetRobot.displayName}."
            } else {
                "Transferência de ${map.name} solicitada à nuvem."
            }
        }
    }

    fun backToDiscovery() = runAction {
        repository.closeRobotSession()
        selectedRobot.value = null
        lastPoint.value = null
        showMapSwitch.value = false
    }

    /** Retorno local ao login. O logout remoto será adicionado com a autenticação da nuvem. */
    fun logout() = runAction {
        repository.closeRobotSession()
        selectedRobot.value = null
        lastPoint.value = null
        showMapSwitch.value = false
        isLoggedIn.value = false
    }

    fun clearError() { errorMessage.value = null }
    fun clearInfo() { infoMessage.value = null }

    override fun onCleared() {
        repository.close()
    }

    private fun runAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            isBusy.value = true
            errorMessage.value = null
            runCatching { block() }.onFailure { errorMessage.value = it.message ?: "Falha inesperada." }
            isBusy.value = false
        }
    }
}

class WellbotViewModelFactory(
    private val repository: WellbotRepository,
    private val authGateway: AuthGateway,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        WellbotViewModel(repository, authGateway) as T
}
