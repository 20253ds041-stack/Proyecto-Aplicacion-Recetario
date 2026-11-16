package mx.edu.utez.mirecetario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import mx.edu.utez.mirecetario.ui.navigation.Route
import mx.edu.utez.mirecetario.ui.theme.MiRecetarioTheme

@Composable
fun BarraNavegacionComponent(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val colorBase = Color(0xFF5476AA)
    val colorSeleccionado = Color(0xFF3B5680)
    fun navegarA(ruta: String) {
        if (rutaActual != ruta) {
            navController.navigate(ruta) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    Column() {
        Row() {
            //en curso
            Surface(
                onClick = { navegarA(Route.RecetaEnCurso.path) },
                modifier = Modifier.weight(1f),
                color = if (rutaActual == Route.RecetaEnCurso.path) colorSeleccionado else colorBase,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Timer, contentDescription = "En curso")
                    Text("Mis Recetas")
                }
            }
            //guardados
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
                    Icon(Icons.Default.Bookmark, contentDescription = "Guardadas")
                    Text("Guardadas")
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