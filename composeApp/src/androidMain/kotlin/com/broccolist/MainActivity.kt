package com.broccolist

import BroccolistTheme.BroccolistTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.luminance
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.broccolist.app.presentation.App
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        viewModel.supabaseClient.handleDeeplinks(intent)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BroccolistTheme {
                val systemUiController = rememberSystemUiController()
                val colorScheme = MaterialTheme.colorScheme
                // Determine if we're in dark theme by checking the background color luminance
                val darkTheme = colorScheme.background.luminance() < 0.5

                // Handle system bars
                DisposableEffect(systemUiController, darkTheme) {
                    systemUiController.setStatusBarColor(
                        color = colorScheme.primaryContainer,
                        darkIcons = !darkTheme
                    )
                    systemUiController.setNavigationBarColor(
                        color = colorScheme.background,
                        darkIcons = !darkTheme,
                        navigationBarContrastEnforced = false
                    )
                    onDispose {}
                }
                App()
            }
        }
    }
}
