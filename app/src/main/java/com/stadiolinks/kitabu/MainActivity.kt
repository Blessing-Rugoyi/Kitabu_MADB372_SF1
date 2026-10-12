package com.stadiolinks.kitabu

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stadiolinks.kitabu.ui.KitabuApp
import com.stadiolinks.kitabu.ui.theme.KitabuTheme

/** The app's only activity; all screens are Compose destinations. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The theme is light-only, so keep the system bar icons dark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            KitabuTheme {
                KitabuApp()
            }
        }
    }
}
