package mx.edu.utez.mirecetario.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.edu.utez.mirecetario.ui.components.BarraNavegacionComponent
import mx.edu.utez.mirecetario.ui.screens.CrearRecetaScreen
import mx.edu.utez.mirecetario.ui.screens.InfoRecetaScreen
import mx.edu.utez.mirecetario.ui.screens.InicioSesionScreen
import mx.edu.utez.mirecetario.ui.screens.PerfilScreen
import mx.edu.utez.mirecetario.ui.screens.RecetaEnCursoScreen
import mx.edu.utez.mirecetario.ui.screens.RecetasGuardadas

sealed class Route(val path: String){
    object CrearReceta : Route("crearReceta")
    object InfoReceta : Route("infoReceta")
    object Perfil: Route("perfil")
    object RecetaEnCurso : Route("recetaEnCurso")
    object InicioSesion : Route("inicioSesion")
    object CrearCuenta : Route("crearCuenta")
    object RecetasGuardadas: Route("recetasGuardadas")
}
@Composable
fun MainAppContent(modifier: Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val pantallasSinBarra = listOf(
        Route.InicioSesion.path,
        Route.CrearCuenta.path,
        Route.InfoReceta.path,
        Route.CrearReceta.path
    )

    Scaffold(
        bottomBar = {
            if (rutaActual !in pantallasSinBarra) {
                BarraNavegacionComponent(navController = navController)
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.InicioSesion.path,
        modifier = modifier
    ) {
        composable(Route.Perfil.path) {
            PerfilScreen(modifier, onIniciarSesion = {navController.navigate(Route.InicioSesion.path)}
            )
        }
        composable(Route.RecetaEnCurso.path){
            RecetaEnCursoScreen(Modifier)
        }
        composable (Route.CrearCuenta.path){

        }
        composable (Route.InfoReceta.path){
            InfoRecetaScreen(
                onBackClick = {navController.popBackStack()},
                onEnCurso = {navController.navigate(Route.RecetaEnCurso)}
            )
        }
        composable (Route.RecetasGuardadas.path){
            RecetasGuardadas(onCrearClick = {
                navController.navigate(Route.CrearReceta.path)
            }
                ,onInfoClick = {navController.navigate(Route.InfoReceta.path)}
            )
        }
        composable(Route.InicioSesion.path){
            InicioSesionScreen(modifier, onIniciadoClick = {navController.navigate(Route.RecetaEnCurso.path)})
        }
        composable(Route.CrearReceta.path){
            CrearRecetaScreen(onBackClick = { navController.popBackStack() })
        }
    }
}