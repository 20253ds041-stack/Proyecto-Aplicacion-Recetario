package mx.edu.utez.mirecetario.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.edu.utez.mirecetario.ui.components.BarraDificultadComponent
import mx.edu.utez.mirecetario.ui.components.ImagenUri
import mx.edu.utez.mirecetario.ui.components.InputsRecetas
import mx.edu.utez.mirecetario.ui.components.TiemposReceta
import mx.edu.utez.mirecetario.ui.util.CampoReceta
import mx.edu.utez.mirecetario.ui.util.rememberDictadoReceta
import mx.edu.utez.mirecetario.ui.viewmodel.CrearRecetaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRecetaScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CrearRecetaViewModel = viewModel()
) {
    val form = viewModel.form
    val fotos = viewModel.fotos
    val dictar = rememberDictadoReceta(form)
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(12)) { uris ->
        viewModel.agregarFotos(uris)
    }
    val abrirSelector = {
        selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Receta", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                expandedHeight = 64.dp,
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sección de Fotos / Multimedia Card con HorizontalPager
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (fotos.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { fotos.size })
                        val scope = rememberCoroutineScope()

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            ImagenUri(fotos[page], Modifier.fillMaxSize(), ContentScale.Crop)
                        }

                        // Indicador de foto actual
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${fotos.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Botón eliminar foto actual
                        IconButton(
                            onClick = { viewModel.quitarFoto(fotos[pagerState.currentPage]) },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar foto", tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                        }

                        // Flecha izquierda < en el borde
                        if (pagerState.currentPage > 0) {
                            IconButton(
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(10.dp)
                                    .size(40.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Anterior",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Flecha derecha > en el borde
                        if (pagerState.currentPage < fotos.size - 1) {
                            IconButton(
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(10.dp)
                                    .size(40.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Siguiente",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Botón agregar fotos
                    IconButton(
                        onClick = { abrirSelector() },
                        modifier = Modifier
                            .size(if (fotos.isEmpty()) 72.dp else 48.dp)
                            .align(if (fotos.isEmpty()) Alignment.Center else Alignment.BottomEnd)
                            .padding(if (fotos.isEmpty()) 0.dp else 12.dp)
                            .background(
                                color = if (fotos.isEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddToPhotos,
                            contentDescription = "Agregar fotos",
                            modifier = Modifier.size(if (fotos.isEmpty()) 36.dp else 24.dp),
                            tint = if (fotos.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Formulario principal en Tarjetas ordenadas
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Título
                    Column {
                        Text(text = "Título de la receta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = form.titulo,
                            onValueChange = { form.titulo = it },
                            singleLine = true,
                            placeholder = { Text("Ej. Tacos al pastor caseros") },
                            trailingIcon = {
                                IconButton(onClick = { dictar(CampoReceta.TITULO) }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Dictar título", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Dificultad
                    Column {
                        Text(text = "Dificultad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BarraDificultadComponent(
                                dificultad = form.dificultad,
                                onDificultadChange = { form.dificultad = it },
                                modifier = Modifier.weight(1f)
                            )
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
                        onMicClick = dictar
                    )

                    InputsRecetas(
                        tituloSeccion = "Ingredientes",
                        placeholderTexto = "Ej. 2 tazas de harina",
                        items = form.ingredientes,
                        onItemChange = { index, nuevoTexto -> form.ingredientes[index] = nuevoTexto },
                        onAgregarItem = { form.ingredientes.add("") },
                        onMicClick = { dictar(CampoReceta.INGREDIENTES) }
                    )

                    InputsRecetas(
                        tituloSeccion = "Instrucciones",
                        placeholderTexto = "Ej. Mezclar los ingredientes en un bowl",
                        items = form.instrucciones,
                        onItemChange = { index, nuevoTexto -> form.instrucciones[index] = nuevoTexto },
                        onAgregarItem = { form.instrucciones.add("") },
                        onMicClick = { dictar(CampoReceta.INSTRUCCIONES) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.guardar(onBackClick) },
                        enabled = !viewModel.guardando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (viewModel.guardando) "Guardando..." else "Guardar receta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
