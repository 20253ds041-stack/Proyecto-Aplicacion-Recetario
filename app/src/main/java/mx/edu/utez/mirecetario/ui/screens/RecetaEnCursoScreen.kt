package mx.edu.utez.mirecetario.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utez.mirecetario.ui.components.BarraDificultadComponent
import mx.edu.utez.mirecetario.ui.components.CabeceraReceta
import mx.edu.utez.mirecetario.ui.components.InputsRecetas
import mx.edu.utez.mirecetario.ui.components.TiemposReceta
import mx.edu.utez.mirecetario.ui.theme.MiRecetarioTheme
import mx.edu.utez.mirecetario.ui.util.RecetaFormState
import mx.edu.utez.mirecetario.ui.viewmodel.RecetaEnCursoViewModel
import mx.edu.utez.mirecetario.ui.viewmodel.RecetaIdFactory

/** [recetaId] <= 0 significa "la última receta iniciada" (guardada en la sesión). */
@Composable
fun RecetaEnCursoScreen(
    modifier: Modifier,
    recetaId: Long = -1L,
    onTerminarNav: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as Application
    val vm: RecetaEnCursoViewModel = viewModel(key = "en_curso_$recetaId", factory = RecetaIdFactory(app, recetaId))
    LaunchedEffect(recetaId) { vm.cargar() }
    val receta = vm.receta

    if (receta != null) {
        val ingredientes = receta.ingredients.ifEmpty { listOf("") }
        val instrucciones = receta.instructions.ifEmpty { listOf("") }
        RecetaEnCursoContenido(
            modifier = modifier,
            titulo = receta.title,
            fotos = receta.photos.map { it.path },
            dificultad = RecetaFormState.nivelAFloat(receta.difficulty),
            prep = receta.prepMinutes.takeIf { it > 0 }?.toString().orEmpty(),
            cook = receta.cookMinutes.takeIf { it > 0 }?.toString().orEmpty(),
            bake = receta.bakeMinutes.takeIf { it > 0 }?.toString().orEmpty(),
            ingredientes = ingredientes,
            instrucciones = instrucciones,
            onTerminar = {
                vm.terminar()
            }
        )
    } else if (vm.cargando) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "No hay receta en curso",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fija una receta tocando el icono de cronómetro en los detalles de cualquier receta para verla aquí.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onTerminarNav,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(50.dp)
            ) {
                Text("Ir a Mis Recetas", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecetaEnCursoContenido(
    modifier: Modifier,
    titulo: String,
    fotos: List<String>,
    dificultad: Float,
    prep: String,
    cook: String,
    bake: String,
    ingredientes: List<String>,
    instrucciones: List<String>,
    onTerminar: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Receta en Curso", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                expandedHeight = 64.dp,
                windowInsets = WindowInsets(0.dp),
                navigationIcon = { },
                actions = {
                    IconButton(onClick = onTerminar) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Terminar Receta",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            //imagen y titulo
            CabeceraReceta(titulo = titulo, onTituloChange = {}, fotos = fotos)

            //Debajo de la imagen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                BarraDificultadComponent(
                    dificultad = dificultad,
                    onDificultadChange = { },
                    habilitado = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                TiemposReceta(
                    prep = prep, cook = cook, bake = bake,
                    onPrepChange = {}, onCookChange = {}, onBakeChange = {},
                    editando = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                //ingredientes
                InputsRecetas(
                    tituloSeccion = "Ingredientes",
                    placeholderTexto = "Escribe el ingrediente",
                    items = ingredientes,
                    onItemChange = { _, _ -> },
                    onAgregarItem = { },
                    editando = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                //instrucciones
                InputsRecetas(
                    tituloSeccion = "Instrucciones",
                    placeholderTexto = "Escribe la instrucción",
                    items = instrucciones,
                    onItemChange = { _, _ -> },
                    onAgregarItem = { },
                    editando = false
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecetaEnCursoPreview() {
    MiRecetarioTheme {
        RecetaEnCursoContenido(
            modifier = Modifier, titulo = "Receta", fotos = emptyList(), dificultad = 0.5f,
            prep = "10", cook = "20", bake = "", ingredientes = listOf("1L Agua"),
            instrucciones = listOf("Hervir agua"), onTerminar = {}
        )
    }
}
