package tech.pd7.wellbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.pd7.wellbot.auth.DemoAuthGateway
import tech.pd7.wellbot.data.WellbotRepositoryFactory
import tech.pd7.wellbot.presentation.WellbotViewModel
import tech.pd7.wellbot.presentation.WellbotViewModelFactory
import tech.pd7.wellbot.robots_area.WellbotApp
import tech.pd7.wellbot.robots_area.theme.WellbotTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy { WellbotRepositoryFactory.create(BuildConfig.MQTT_BROKER_URI) }
    private val authGateway by lazy { DemoAuthGateway() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WellbotTheme {
                val viewModel: WellbotViewModel = viewModel(
                    factory = WellbotViewModelFactory(repository, authGateway),
                )
                WellbotApp(viewModel)
            }
        }
    }
}
