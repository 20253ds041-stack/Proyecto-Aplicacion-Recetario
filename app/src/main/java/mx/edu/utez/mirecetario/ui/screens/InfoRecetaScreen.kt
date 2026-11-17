package mx.edu.utez.mirecetario.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.mirecetario.ui.components.BarraDificultadComponent
import mx.edu.utez.mirecetario.ui.components.InputsRecetas
import androidx.compose.ui.text.TextStyle
import mx.edu.utez.mirecetario.ui.components.Alerta


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoRecetaScreen(
    onBackClick: () -> Unit = {},
    onEnCurso: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    //Estas variables mientras no esta el viewmodel
    var titulo by remember { mutableStateOf("titulo de la receta") }
    val ingredientes = remember { mutableStateListOf("1L Agua") }
    val instrucciones = remember { mutableStateListOf("hervir agua") }
    var editando by remember { mutableStateOf(false) }
    var dificultad by remember { mutableFloatStateOf(0.5f) }
    var borrar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
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
                    IconButton(onClick = onEnCurso) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Iniciar Receta",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = { borrar = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = { editando = !editando }) {
                        Icon(
                            imageVector = if (editando) Icons.Default.EditOff else Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF5476AA),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8EEF5)),
                contentAlignment = Alignment.Center
            ) {
                //aca las imagenes :v
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            readOnly = !editando,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (editando) Color(0xFF5476AA) else Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent
                            ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        //cambiar imagen
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Atras"
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Atras"
                            )
                        }
                    }
                }
            }

            //Debajo de la imagen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Codigo de la receta: ")

                Spacer(modifier = Modifier.height(12.dp))

                BarraDificultadComponent(
                    dificultad = dificultad,
                    onDificultadChange = { dificultad = it },
                    habilitado = editando
                )

                Spacer(modifier = Modifier.height(16.dp))

                //ingredientes
                InputsRecetas(
                    tituloSeccion = "Ingredientes",
                    placeholderTexto = "Escribe el ingrediente",
                    items = ingredientes,
                    onItemChange = { index, nuevoTexto -> ingredientes[index] = nuevoTexto },
                    onAgregarItem = { ingredientes.add("") },
                    editando = editando
                )

                Spacer(modifier = Modifier.height(16.dp))

                //instrucciones
                InputsRecetas(
                    tituloSeccion = "Instrucciones",
                    placeholderTexto = "Escribe la instrucción",
                    items = instrucciones,
                    onItemChange = { index, nuevoTexto -> instrucciones[index] = nuevoTexto },
                    onAgregarItem = { instrucciones.add("") },
                    editando = editando
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
            if (borrar) {
                Alerta(
                    titulo = "¿Estás seguro de borrar esta receta?",
                    subtitulo = "Esta acción no se puede deshacer.",
                    textoConfirmar = "Borrar",
                    onConfirmar = {
                        borrar = false
                        onBackClick()
                    },
                    onCancelar = { borrar = false }
                )
            }
        }
    }
}

