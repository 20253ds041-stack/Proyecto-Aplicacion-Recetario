package mx.edu.utez.mirecetario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/** Imagen (con carrusel HorizontalPager y botones en bordes) y título de la receta. */
@Composable
fun CabeceraReceta(
    titulo: String,
    onTituloChange: (String) -> Unit,
    fotos: List<String>,
    modifier: Modifier = Modifier,
    editando: Boolean = false,
    onMicTitulo: () -> Unit = {},
    onAgregarFoto: () -> Unit = {},
    onEliminarFoto: (Int) -> Unit = {}
) {
    val total = fotos.size
    val pagerState = rememberPagerState(pageCount = { total })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (total > 0) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ImagenReceta(
                    rutaRelativa = fotos[page],
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Indicador de foto actual (ej. 1 / 2)
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / $total",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
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
            if (pagerState.currentPage < total - 1) {
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

        if (editando) {
            Row(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                IconButton(
                    onClick = onAgregarFoto,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Default.AddToPhotos, contentDescription = "Agregar fotos", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                if (total > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onEliminarFoto(pagerState.currentPage) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Quitar foto", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = onTituloChange,
                    readOnly = !editando,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    trailingIcon = if (editando) {
                        {
                            IconButton(onClick = onMicTitulo) {
                                Icon(Icons.Default.Mic, contentDescription = "Dictar título", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (editando) MaterialTheme.colorScheme.primary else Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    ),
                )
            }
        }
    }
}
