package mx.edu.utez.mirecetario.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.mirecetario.ui.components.Alerta
import mx.edu.utez.mirecetario.ui.components.ImagenReceta
import mx.edu.utez.mirecetario.ui.theme.MiRecetarioTheme

val availableColors = listOf(
    Color(0xFF5476AA) to "Azul (Predeterminado)",
    Color(0xFF388E3C) to "Verde",
    Color(0xFF7B1FA2) to "Morado",
    Color(0xFFC2185B) to "Rosa",
    Color(0xFFE65100) to "Naranja",
    Color(0xFF00796B) to "Turquesa",
    Color(0xFF455A64) to "Azul grisáceo",
    Color(0xFFB71C1C) to "Rojo"
)

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    nombre: String = "",
    correo: String = "",
    avatarPath: String? = null,
    currentPrimaryColor: Color = Color(0xFF5476AA),
    onColorChanged: (Color) -> Unit = {},
    onCambiarAvatar: (Uri) -> Unit = {},
    onEliminarAvatar: () -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    var cerrar by remember { mutableStateOf(false) }
    var mostrarOpcionesAvatar by remember { mutableStateOf(false) }

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onCambiarAvatar(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { mostrarOpcionesAvatar = true },
                contentAlignment = Alignment.Center
            ) {
                if (!avatarPath.isNullOrBlank()) {
                    ImagenReceta(
                        rutaRelativa = avatarPath,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil por defecto",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = nombre.ifEmpty { "Usuario" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Toca el avatar para cambiar foto",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Input de correo
        Text("Correo electrónico", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = correo,
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))


        // Sección Configuración / Color principal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configuración: Color principal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Elige el color de acento para la aplicación",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Círculos de colores (muestras)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableColors.take(4).forEach { (color, name) ->
                        ColorSwatchItem(
                            color = color,
                            name = name,
                            isSelected = color == currentPrimaryColor,
                            onClick = { onColorChanged(color) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableColors.drop(4).forEach { (color, name) ->
                        ColorSwatchItem(
                            color = color,
                            name = name,
                            isSelected = color == currentPrimaryColor,
                            onClick = { onColorChanged(color) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onColorChanged(Color(0xFF5476AA)) },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restaurar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restaurar predeterminado (Azul)")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // btn cerrar sesión
        Button(
            onClick = { cerrar = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        if (mostrarOpcionesAvatar) {
            AlertDialog(
                onDismissRequest = { mostrarOpcionesAvatar = false },
                title = {
                    Text(
                        text = "Foto de perfil",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                mostrarOpcionesAvatar = false
                                selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (avatarPath.isNullOrBlank()) "Añadir foto" else "Cambiar foto")
                        }
                        if (!avatarPath.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    mostrarOpcionesAvatar = false
                                    onEliminarAvatar()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                            ) {
                                Text("Eliminar foto")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { mostrarOpcionesAvatar = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (cerrar) {
            Alerta(
                titulo = "¿Estás seguro de cerrar sesión?",
                textoConfirmar = "Salir",
                onConfirmar = {
                    cerrar = false
                    onCerrarSesion()
                },
                onCancelar = {
                    cerrar = false
                }
            )
        }
    }
}

@Composable
fun ColorSwatchItem(
    color: Color,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilScreenPreview() {
    MiRecetarioTheme {
        PerfilScreen(
            modifier = Modifier.padding(0.dp),
            nombre = "Samantha",
            correo = "samantha@gmail.com",
            onCerrarSesion = {}
        )
    }
}
