package mx.edu.utez.mirecetario.ui.screens

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utez.mirecetario.ui.components.Alerta
import mx.edu.utez.mirecetario.ui.components.BarraDificultadComponent
import mx.edu.utez.mirecetario.ui.components.CabeceraReceta
import mx.edu.utez.mirecetario.ui.components.InputsRecetas
import mx.edu.utez.mirecetario.ui.components.TiemposReceta
import mx.edu.utez.mirecetario.ui.util.CampoReceta
import mx.edu.utez.mirecetario.ui.util.rememberDictadoReceta
import mx.edu.utez.mirecetario.ui.viewmodel.RecetaDetalleViewModel
import mx.edu.utez.mirecetario.ui.viewmodel.RecetaIdFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoRecetaScreen(
    recetaId: Long,
    onBackClick: () -> Unit = {},
    onEnCurso: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val vm: RecetaDetalleViewModel = viewModel(key = "receta_$recetaId", factory = RecetaIdFactory(app, recetaId))
    LaunchedEffect(recetaId) { vm.cargar() }

    val form = vm.form
    val editando = vm.editando
    var borrar by remember { mutableStateOf(false) }
    val dictar = rememberDictadoReceta(form)

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(12)) { uris ->
        if (uris.isNotEmpty()) vm.agregarFotos(uris)
    }
    val exportar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) vm.exportar(uri)
    }
    val receta = vm.receta

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (editando) "Editando" else "Detalles", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                expandedHeight = 64.dp,
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                actions = {
                    if (receta != null) {
                        IconButton(onClick = { onEnCurso(receta.id) }) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Iniciar Receta",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { borrar = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = {
                            val nombre = receta.title.replace(Regex("[^\\p{L}\\p{N} _-]"), "_").trim().ifEmpty { "receta" }
                            exportar.launch("$nombre.zip")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir (exportار ZIP)",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (editando) {
                            IconButton(onClick = { vm.cancelarEdicion() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar edición",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        IconButton(onClick = { vm.alternarEdicion() }) {
                            Icon(
                                imageVector = if (editando) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = if (editando) "Guardar cambios" else "Editar",
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (receta == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                if (vm.cargando) CircularProgressIndicator()
                else Text(if (vm.noEncontrada) "Receta no encontrada" else "No se pudo cargar la receta", fontSize = 18.sp)
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera multimedia y título
            CabeceraReceta(
                titulo = form.titulo,
                onTituloChange = { form.titulo = it },
                fotos = receta.photos.map { it.path },
                editando = editando,
                onMicTitulo = { dictar(CampoReceta.TITULO) },
                onAgregarFoto = {
                    selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onEliminarFoto = { posicion -> receta.photos.getOrNull(posicion)?.let { vm.eliminarFoto(it.id) } }
            )

            // Contenido organizado de la receta en Tarjetas elegantes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de Resumen / Dificultad y Tiempos
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (receta.totalMinutes > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "⏱ Tiempo total estimado: ${receta.totalMinutes} min",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Text(text = "Dificultad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        BarraDificultadComponent(
                            dificultad = form.dificultad,
                            onDificultadChange = { form.dificultad = it },
                            habilitado = editando
                        )
                        if (editando) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Dictar dificultad (fácil, media o difícil)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = { dictar(CampoReceta.DIFICULTAD) }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Dictar dificultad", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        TiemposReceta(
                            prep = form.prep, cook = form.cook, bake = form.bake,
                            onPrepChange = { form.prep = it },
                            onCookChange = { form.cook = it },
                            onBakeChange = { form.bake = it },
                            editando = editando,
                            onMicClick = dictar
                        )
                    }
                }

                // Tarjeta de Ingredientes
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        InputsRecetas(
                            tituloSeccion = "Ingredientes",
                            placeholderTexto = "Escribe el ingrediente",
                            items = form.ingredientes,
                            onItemChange = { index, nuevoTexto -> form.ingredientes[index] = nuevoTexto },
                            onAgregarItem = { form.ingredientes.add("") },
                            editando = editando,
                            onMicClick = { dictar(CampoReceta.INGREDIENTES) }
                        )
                    }
                }

                // Tarjeta de Instrucciones
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        InputsRecetas(
                            tituloSeccion = "Instrucciones",
                            placeholderTexto = "Escribe la instrucción",
                            items = form.instrucciones,
                            onItemChange = { index, nuevoTexto -> form.instrucciones[index] = nuevoTexto },
                            onAgregarItem = { form.instrucciones.add("") },
                            editando = editando,
                            onMicClick = { dictar(CampoReceta.INSTRUCCIONES) }
                        )
                    }
                }
            }

            if (borrar) {
                Alerta(
                    titulo = "¿Estás seguro de borrar esta receta?",
                    subtitulo = "Esta acción no se puede deshacer.",
                    textoConfirmar = "Borrar",
                    onConfirmar = {
                        borrar = false
                        vm.eliminar(onBackClick)
                    },
                    onCancelar = { borrar = false }
                )
            }
        }
    }
}

