package mx.edu.utez.mirecetario.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.mirecetario.ui.components.BarraDificultadComponent
import mx.edu.utez.mirecetario.ui.components.InputsRecetas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRecetaScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    //Estas variables mientras no esta el viewmodel
    val ingredientes = remember { mutableStateListOf("") }
    val instrucciones = remember { mutableStateListOf("") }
    var dificultad by remember { mutableFloatStateOf(0.5f) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crear Receta", fontSize = (54.sp)) },
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
                    Color.White,
                    Color.White,
                    Color.White,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(350.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8EEF5)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    {},
                    modifier = Modifier.size(84.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddToPhotos,
                        contentDescription = "Agregar fotos",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = "Titulo", fontSize = 32.sp)
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    singleLine = true,
                    label = { },
                    placeholder = { Text("Ingrese el titulo de la receta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(10.dp))
                //dificultad
                Row {
                    Text(text = "Dificultad", fontSize = 32.sp)
                    BarraDificultadComponent(
                        dificultad = dificultad,
                        onDificultadChange = { dificultad = it }
                    )
                }
                //ingredientes
                InputsRecetas(
                    tituloSeccion = "Ingredientes",
                    placeholderTexto = "Escribe el ingrediente",
                    items = ingredientes,
                    onItemChange = { index, nuevoTexto -> ingredientes[index] = nuevoTexto },
                    onAgregarItem = { ingredientes.add("") }
                )

                Spacer(modifier = Modifier.height(16.dp))
                //Instrucciones
                InputsRecetas(
                    tituloSeccion = "Instrucciones",
                    placeholderTexto = "Escribe la instrucción",
                    items = instrucciones,
                    onItemChange = { index, nuevoTexto -> instrucciones[index] = nuevoTexto },
                    onAgregarItem = { instrucciones.add("") }
                )

            }
        }
    }
}