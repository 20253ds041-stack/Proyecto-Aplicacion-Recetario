package mx.edu.utez.mirecetario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import mx.edu.utez.mirecetario.ui.navigation.Route

@Composable
fun BarraNavegacionComponent(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val colorBase = MaterialTheme.colorScheme.primary
    val colorSeleccionado = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    fun esMismaRuta(actual: String?, destino: String): Boolean {
        if (actual == null) return false
        if (actual == destino) return true
        if (destino == Route.RecetaEnCurso.path && actual.startsWith(Route.RecetaEnCurso.path)) return true
        return false
    }

    fun navegarA(ruta: String) {
        if (!esMismaRuta(rutaActual, ruta)) {
            navController.navigate(ruta) {
                popUpTo(Route.RecetasGuardadas.path) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }
    Column {
        Row {
            //en curso
            Surface(
                onClick = { navegarA(Route.RecetaEnCurso.path) },
                modifier = Modifier.weight(1f),
                color = if (rutaActual?.startsWith(Route.RecetaEnCurso.path) == true) colorSeleccionado else colorBase,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Timer, contentDescription = "En curso")
                    Text("En curso")
                }
            }
            //guardados (Mis Recetas)
            Surface(
                onClick = { navegarA(Route.RecetasGuardadas.path) },
                modifier = Modifier.weight(1f),
                color = if (rutaActual == Route.RecetasGuardadas.path) colorSeleccionado else colorBase,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Mis Recetas")
                    Text("Mis Recetas")
                }
            }
            //perfil
            Surface(
                onClick = { navegarA(Route.Perfil.path) },
                modifier = Modifier.weight(1f),
                color = if (rutaActual == Route.Perfil.path) colorSeleccionado else colorBase,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                    Text("Perfil")
                }
            }
        }
    }
}
