package mx.edu.utez.mirecetario.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utez.mirecetario.ui.components.BarraNavegacionComponent
import mx.edu.utez.mirecetario.ui.screens.CrearCuentaScreen
import mx.edu.utez.mirecetario.ui.screens.CrearRecetaScreen
import mx.edu.utez.mirecetario.ui.screens.InfoRecetaScreen
import mx.edu.utez.mirecetario.ui.screens.InicioSesionScreen
import mx.edu.utez.mirecetario.ui.screens.PerfilScreen
import mx.edu.utez.mirecetario.ui.screens.RecetaEnCursoScreen
import mx.edu.utez.mirecetario.ui.screens.RecetasGuardadas
import mx.edu.utez.mirecetario.ui.viewmodel.SesionEstado
import mx.edu.utez.mirecetario.ui.viewmodel.SessionViewModel

sealed class Route(val path: String) {
    object CrearReceta : Route("crearReceta")
    object InfoReceta : Route("infoReceta") {
        const val ARG_ID = "id"
        val pattern = "infoReceta/{$ARG_ID}"
        fun conId(id: Long) = "infoReceta/$id"
    }
    object Perfil : Route("perfil")
    object RecetaEnCurso : Route("recetaEnCurso") {
        const val ARG_ID = "id"
        /** El id es opcional: sin él se muestra la última receta iniciada. */
        val pattern = "recetaEnCurso?$ARG_ID={$ARG_ID}"
        fun conId(id: Long) = "recetaEnCurso?$ARG_ID=$id"
    }
    object InicioSesion : Route("inicioSesion")
    object CrearCuenta : Route("crearCuenta")
    object RecetasGuardadas : Route("recetasGuardadas")
}

@Composable
fun MainAppContent(
    modifier: Modifier,
    currentPrimaryColor: Color = Color(0xFF5476AA),
    onColorChanged: (Color) -> Unit = {},
    sesion: SessionViewModel = viewModel()
) {
    val comprobando = sesion.estado is SesionEstado.Cargando
    if (comprobando) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    // La pantalla inicial se decide una sola vez, cuando termina de comprobarse la sesión guardada.
    val inicio = remember {
        if (sesion.estado is SesionEstado.Activa) Route.RecetasGuardadas.path else Route.InicioSesion.path
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val pantallasSinBarra = listOf(
        Route.InicioSesion.path,
        Route.CrearCuenta.path
    )

    // Se conserva el comportamiento original: el Scaffold exterior no se aplica aquí (evita duplicar insets).
    // Si la sesión se cierra (p. ej. caducó el token), se vuelve al inicio de sesión.
    LaunchedEffect(sesion.estado, rutaActual) {
        if (sesion.estado is SesionEstado.Cerrada && rutaActual != null &&
            rutaActual != Route.InicioSesion.path && rutaActual != Route.CrearCuenta.path
        ) {
            navController.navigate(Route.InicioSesion.path) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (rutaActual !in pantallasSinBarra) {
                BarraNavegacionComponent(navController = navController)
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            sesion = sesion,
            startDestination = inicio,
            currentPrimaryColor = currentPrimaryColor,
            onColorChanged = onColorChanged,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    sesion: SessionViewModel,
    startDestination: String,
    currentPrimaryColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    fun irAInicio() = navController.navigate(Route.RecetasGuardadas.path) {
        popUpTo(0) { inclusive = true }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.Perfil.path) {
            val usuario = (sesion.estado as? SesionEstado.Activa)?.user
            PerfilScreen(
                nombre = usuario?.name.orEmpty(),
                correo = usuario?.email.orEmpty(),
                avatarPath = usuario?.avatarPath,
                currentPrimaryColor = currentPrimaryColor,
                onColorChanged = onColorChanged,
                onCambiarAvatar = { sesion.subirAvatar(it) },
                onEliminarAvatar = { sesion.eliminarAvatar() },
                onCerrarSesion = {
                    sesion.cerrarSesion {
                        navController.navigate(Route.InicioSesion.path) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(
            route = Route.RecetaEnCurso.pattern,
            arguments = listOf(navArgument(Route.RecetaEnCurso.ARG_ID) {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            RecetaEnCursoScreen(
                modifier = Modifier,
                recetaId = entry.arguments?.getLong(Route.RecetaEnCurso.ARG_ID) ?: -1L,
                onTerminarNav = {
                    navController.navigate(Route.RecetasGuardadas.path) {
                        popUpTo(Route.RecetasGuardadas.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Route.CrearCuenta.path) {
            LaunchedEffect(Unit) { sesion.limpiarError() }
            CrearCuentaScreen(
                onCrearCuentaClick = { nombre, correo, clave ->
                    sesion.registrar(nombre, correo, clave) { irAInicio() }
                },
                onVolverClick = { navController.popBackStack() },
                cargando = sesion.trabajando,
                error = sesion.error
            )
        }
        composable(
            route = Route.InfoReceta.pattern,
            arguments = listOf(navArgument(Route.InfoReceta.ARG_ID) { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong(Route.InfoReceta.ARG_ID) ?: -1L
            InfoRecetaScreen(
                recetaId = id,
                onBackClick = { navController.popBackStack() },
                onEnCurso = { recetaId ->
                    navController.navigate(Route.RecetaEnCurso.conId(recetaId)) {
                        popUpTo(Route.RecetasGuardadas.path) {
                            saveState = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Route.RecetasGuardadas.path) {
            RecetasGuardadas(
                onCrearClick = { navController.navigate(Route.CrearReceta.path) },
                onInfoClick = { id -> navController.navigate(Route.InfoReceta.conId(id)) }
            )
        }
        composable(Route.InicioSesion.path) {
            LaunchedEffect(Unit) { sesion.limpiarError() }
            InicioSesionScreen(
                onIniciarClick = { correo, clave -> sesion.iniciarSesion(correo, clave) { irAInicio() } },
                onCrearCuentaClick = { navController.navigate(Route.CrearCuenta.path) },
                cargando = sesion.trabajando,
                error = sesion.error
            )
        }
        composable(Route.CrearReceta.path) {
            CrearRecetaScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
