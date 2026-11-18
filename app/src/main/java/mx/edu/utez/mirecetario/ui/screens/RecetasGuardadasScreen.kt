package mx.edu.utez.mirecetario.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.edu.utez.mirecetario.ui.components.BarraDificultadComponent


data class RecetaUI(
    val id: String,
    val titulo: String,
    val etiqueta: String,
    val dificultad: Int,
    val imagenes: List<Int>,
    val progreso: Float
)

@Composable
fun RecetasGuardadasScreen(
    recetas: List<RecetaUI>,
    onCrearClick: () -> Unit,
    onRecetaClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    //variables temporales en lo que se crea el viewModel
    var busqueda by remember { mutableStateOf("") }
    val azulPrincipal = Color(0xFF5476AA)

    val recetasFiltradas = remember(busqueda, recetas) {
        if (busqueda.isBlank()) recetas
        else recetas.filter { it.titulo.contains(busqueda, ignoreCase = true) }
    }

    Scaffold(
        bottomBar = bottomBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Título centrado
            Text(
                text = "Recetas Guardadas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = azulPrincipal,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Area de Buscador y el Botón  para crear
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar...") },
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onCrearClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = azulPrincipal),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text("Crear", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Crear receta")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de tarjetas de recetas
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
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

@Composable
fun RecetaGuardadaCard(
    receta: RecetaUI,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila Título y Etiqueta roja
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receta.titulo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5476AA)
                )

                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = receta.etiqueta,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fila Dificultad
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Dificultad: ",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                BarraDificultadComponent(
                    dificultad = receta.dificultad/3f,
                    onDificultadChange = { },
                    habilitado = false
                )
                Text(
                    text = "Nivel ${receta.dificultad}/3",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Imagen O Carrusel  si hay más de 1 imagen
            RecetaMediaViewer(imagenes = receta.imagenes)

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de progreso inferior de la tarjeta
            LinearProgressIndicator(
                progress = { receta.progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF5476AA),
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun RecetaMediaViewer(imagenes: List<Int>) {
    if (imagenes.isEmpty()) return

    if (imagenes.size == 1) {
        // Caso 1: Solo una imagen fija
        Image(
            painter = painterResource(id = imagenes.first()),
            contentDescription = "Imagen de receta",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        // Caso 2: Más de una imagen Carrusel con flechas < >
        val pagerState = rememberPagerState(pageCount = { imagenes.size })
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Image(
                    painter = painterResource(id = imagenes[page]),
                    contentDescription = "Imagen ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
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
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Anterior",
                        tint = Color.White
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
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Siguiente",
                        tint = Color.White
                    )
                }
            }
        }
    }
}