package mx.edu.utez.mirecetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import mx.edu.utez.mirecetario.data.repository.SessionStore
import mx.edu.utez.mirecetario.ui.navigation.MainAppContent
import mx.edu.utez.mirecetario.ui.theme.MiRecetarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionStore = remember { SessionStore(applicationContext) }
            var primaryColor by remember { mutableStateOf(Color(sessionStore.primaryColorInt)) }

            MiRecetarioTheme(primaryColor = primaryColor) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainAppContent(
                        modifier = Modifier.padding(innerPadding),
                        currentPrimaryColor = primaryColor,
                        onColorChanged = { newColor ->
                            sessionStore.primaryColorInt = newColor.toArgb()
                            primaryColor = newColor
                        }
                    )
                }
            }
        }
    }
}
