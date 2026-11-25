package mx.edu.utez.mirecetario.ui.screens

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utez.mirecetario.data.remote.RecipeDto
import mx.edu.utez.mirecetario.ui.components.ImagenReceta
import mx.edu.utez.mirecetario.ui.viewmodel.RecetasGuardadasViewModel

data class RecetaUI(
    val id: Long,
    val titulo: String,
    val etiqueta: String,
    val dificultad: Int,
    val imagenes: List<String>,
    val progreso: Float? = null
)

fun RecipeDto.toUi() = RecetaUI(
    id = id,
    titulo = title,
    etiqueta = if (totalMinutes > 0) "$totalMinutes min" else "Sin tiempo",
    dificultad = difficulty,
    imagenes = photos.map { it.path }
)

@Composable
fun RecetasGuardadas(
    onCrearClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    viewModel: RecetasGuardadasViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.cargar() }
    val importar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importar(uri)
    }
    val recetas = viewModel.recetas.map { it.toUi() }
    RecetasGuardadasScreen(
        recetas = recetas,
        onCrearClick = onCrearClick,
        onRecetaClick = onInfoClick,
        onImportarClick = {
            importar.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
        }
    )
}

@Composable
fun RecetasGuardadasScreen(
    recetas: List<RecetaUI>,
    onCrearClick: () -> Unit,
    onRecetaClick: (Long) -> Unit,
    onImportarClick: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var busqueda by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    val recetasFiltradas = remember(busqueda, recetas) {
        if (busqueda.isBlank()) recetas
        else recetas.filter { it.titulo.contains(busqueda, ignoreCase = true) }
    }

    Scaffold(
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Título principal con diseño moderno y elegante
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mis Recetas",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (recetas.isEmpty()) "Comienza a crear tu recetario" else "${recetas.size} recetas guardadas",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onCrearClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Crear receta", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nueva", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Buscador e Importar en fila armoniosa
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar receta", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = primaryColor)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = onImportarClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Importar receta ZIP",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Lista de recetas o estado vacío
            if (recetasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (busqueda.isBlank()) "No tienes recetas guardadas" else "No se encontraron resultados",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (busqueda.isBlank()) "Toca el botón 'Nueva' para agregar tu primera receta o importa un archivo ZIP." else "Prueba buscando con otro término.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(recetasFiltradas, key = { it.id }) { receta ->
                        RecetaGuardadaCard(
                            receta = receta,
                            onClick = { onRecetaClick(receta.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecetaGuardadaCard(
    receta: RecetaUI,
    onClick: () -> Unit
) {
    val dificultadTexto = when (receta.dificultad) {
        1 -> "Fácil"
        2 -> "Media"
        else -> "Difícil"
    }
    val dificultadColor = when (receta.dificultad) {
        1 -> Color(0xFF2E7D32) // Verde
        2 -> Color(0xFFFFC107) // Amarillo
        else -> Color(0xFFC62828) // Rojo
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Cabecera de la tarjeta: Título y tiempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = receta.titulo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Tiempo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = receta.etiqueta,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dificultad badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = dificultadColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Dificultad: $dificultadTexto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = dificultadColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Imagen o carrusel multimedia con excelente aspecto
            if (receta.imagenes.isNotEmpty()) {
                RecetaMediaViewer(imagenes = receta.imagenes)
            }

            receta.progreso?.let { progreso ->
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecetaMediaViewer(imagenes: List<String>) {
    if (imagenes.isEmpty()) return

    if (imagenes.size == 1) {
        ImagenReceta(
            rutaRelativa = imagenes.first(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(18.dp))
        )
    } else {
        val pagerState = rememberPagerState(pageCount = { imagenes.size })
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ImagenReceta(
                    rutaRelativa = imagenes[page],
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Indicador de foto actual (ej. 1/2)
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${imagenes.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Flecha izquierda <
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Anterior",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Flecha derecha >
            if (pagerState.currentPage < imagenes.size - 1) {
                IconButton(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Siguiente",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
