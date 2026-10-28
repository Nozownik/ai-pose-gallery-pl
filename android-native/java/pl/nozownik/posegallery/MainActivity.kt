package pl.nozownik.posegallery

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import pl.nozownik.posegallery.data.AppThemeId
import pl.nozownik.posegallery.ui.AppRoot
import pl.nozownik.posegallery.ui.PoseTheme
import pl.nozownik.posegallery.ui.PoseViewModel
import pl.nozownik.posegallery.util.ShortcutHelper

class MainActivity : ComponentActivity() {
    private val vm: PoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val state by vm.uiState.collectAsState()
            val theme = state.settings.theme
            val light = theme == AppThemeId.LIGHT
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = light
            PoseTheme(themeId = theme) {
                LaunchedEffect(state.loading, intent?.action) {
                    if (!state.loading && intent?.action == ShortcutHelper.ACTION_RANDOM) {
                        vm.openRandomPose()
                        intent?.action = Intent.ACTION_MAIN
                    }
                }
                AppRoot(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ShortcutHelper.ACTION_RANDOM && !vm.uiState.value.loading) {
            vm.openRandomPose()
        }
    }
}
