package pl.nozownik.posegallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import pl.nozownik.posegallery.ui.AppRoot
import pl.nozownik.posegallery.ui.PoseTheme
import pl.nozownik.posegallery.ui.PoseViewModel

class MainActivity : ComponentActivity() {
    private val vm: PoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            PoseTheme {
                AppRoot(vm)
            }
        }
    }
}
