package tech.pd7.wellbot.robots_area

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.pd7.wellbot.models.NavigationPoint
import tech.pd7.wellbot.models.Robot
import tech.pd7.wellbot.models.RobotMap
import tech.pd7.wellbot.presentation.WellbotUiState
import tech.pd7.wellbot.presentation.WellbotViewModel

private val OnlineGreen = Color(0xFF57F287)
private val OfflineRed = Color(0xFFE44848)

@Composable
fun WellbotApp(viewModel: WellbotViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage, state.infoMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
        state.infoMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearInfo()
        }
    }

    if (!state.isLoggedIn) {
        LoginScreen(state.isBusy, viewModel::login)
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (state.selectedRobot != null) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::backToDiscovery,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text("TROCAR ROBÔ", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.selectedRobot == null) {
                DiscoveryScreen(state, viewModel::selectRobot, viewModel::logout)
            } else {
                NavigationScreen(
                    state = state,
                    onPoint = viewModel::send,
                    onOpenMapSwitch = viewModel::openMapSwitch,
                    onLogout = viewModel::logout,
                )
            }
        }
    }

    if (state.showMapSwitch) {
        MapSwitchDialog(
            state = state,
            onDismiss = viewModel::closeMapSwitch,
            onTransfer = viewModel::transferMap,
        )
    }
}

@Composable
private fun LoginScreen(isBusy: Boolean, onLogin: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "WELLBOT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                )
                Text("ACESSO DO OPERADOR", fontFamily = FontFamily.Monospace)
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuário") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { onLogin(username, password) },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("ENTRAR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Login demonstrativo: nenhuma senha é validada ou armazenada.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DiscoveryScreen(
    state: WellbotUiState,
    onSelect: (Robot) -> Unit,
    onLogout: () -> Unit,
) {
    ScreenTitle("ROBÔS NA NUVEM", onLogout)
    Text(
        if (state.isDemo) "MODO DEMONSTRAÇÃO" else "ONLINE E OFFLINE",
        fontFamily = FontFamily.Monospace,
    )
    if (state.isBusy && state.robots.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (state.robots.isEmpty()) {
        Text("Nenhum SN disponível no broker.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.robots, key = Robot::serialNumber) { robot ->
            RobotCard(robot, onSelect)
        }
    }
}

@Composable
private fun RobotCard(robot: Robot, onSelect: (Robot) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(robot.displayName, fontWeight = FontWeight.Bold)
                Text(
                    "SN ${robot.serialNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Button(
                onClick = { onSelect(robot) },
                enabled = robot.online,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnlineGreen,
                    contentColor = Color(0xFF07110B),
                    disabledContainerColor = OfflineRed,
                    disabledContentColor = Color.White,
                ),
            ) {
                Text(if (robot.online) "ONLINE" else "OFFLINE", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun NavigationScreen(
    state: WellbotUiState,
    onPoint: (NavigationPoint) -> Unit,
    onOpenMapSwitch: () -> Unit,
    onLogout: () -> Unit,
) {
    val robot = state.selectedRobot ?: return
    ScreenTitle("ÁREA DE NAVEGAÇÃO", onLogout)
    CompactRobotHeader(robot)
    OutlinedButton(
        onClick = onOpenMapSwitch,
        enabled = !state.isBusy,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text("SWITCH DE MAPA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
    Text("POINTS DE NAVEGAÇÃO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    if (state.navigationPoints.isEmpty()) {
        Text("Aguardando os points configurados pelo robô na nuvem.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.navigationPoints, key = NavigationPoint::id) { point ->
                Button(
                    onClick = { onPoint(point) },
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(point.label.uppercase(), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    state.lastPoint?.let {
        Text("ÚLTIMO POINT // ${it.label}", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ScreenTitle(title: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
        )
        TextButton(onClick = onLogout) {
            Text("SAIR", color = OfflineRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactRobotHeader(robot: Robot) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(robot.displayName, fontWeight = FontWeight.Bold)
                Text(
                    "SN ${robot.serialNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Box(
                Modifier.size(12.dp).background(
                    color = if (robot.online) OnlineGreen else OfflineRed,
                    shape = CircleShape,
                ),
            )
        }
    }
}

@Composable
private fun MapSwitchDialog(
    state: WellbotUiState,
    onDismiss: () -> Unit,
    onTransfer: (RobotMap, Robot) -> Unit,
) {
    val sourceRobot = state.selectedRobot ?: return
    val targets = state.robots.filter { it.online && it.serialNumber != sourceRobot.serialNumber }
    var selectedMap by remember(state.maps) { mutableStateOf<RobotMap?>(null) }
    var selectedTarget by remember(targets) { mutableStateOf<Robot?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SWITCH DE MAPA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("Mapa de ${sourceRobot.displayName}", fontWeight = FontWeight.Bold) }
                if (state.maps.isEmpty()) {
                    item { Text("Nenhum mapa publicado por este robô.") }
                } else {
                    items(state.maps, key = RobotMap::id) { map ->
                        SelectionButton(
                            text = map.name + (map.version?.let { " · v$it" } ?: ""),
                            selected = selectedMap == map,
                            onClick = { selectedMap = map },
                        )
                    }
                }
                item { Text("Robô de destino", fontWeight = FontWeight.Bold) }
                if (targets.isEmpty()) {
                    item { Text("Nenhum outro robô online disponível.") }
                } else {
                    items(targets, key = Robot::serialNumber) { robot ->
                        SelectionButton(
                            text = robot.displayName,
                            selected = selectedTarget == robot,
                            onClick = { selectedTarget = robot },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTransfer(requireNotNull(selectedMap), requireNotNull(selectedTarget)) },
                enabled = selectedMap != null && selectedTarget != null && !state.isBusy,
            ) {
                Text("COPIAR E INSTALAR")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
    )
}

@Composable
private fun SelectionButton(text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
    }
}
