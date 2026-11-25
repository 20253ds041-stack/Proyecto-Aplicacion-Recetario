package mx.edu.utez.mirecetario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.mirecetario.ui.util.CampoReceta

/** Tiempos de preparación, cocción y horneado (en minutos). Vacío = no aplica. */
@Composable
fun TiemposReceta(
    prep: String,
    cook: String,
    bake: String,
    onPrepChange: (String) -> Unit,
    onCookChange: (String) -> Unit,
    onBakeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    editando: Boolean = true,
    onMicClick: (CampoReceta) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Tiempos", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
        FilaTiempo("Preparación", prep, onPrepChange, editando) { onMicClick(CampoReceta.PREPARACION) }
        FilaTiempo("Cocción", cook, onCookChange, editando) { onMicClick(CampoReceta.COCCION) }
        FilaTiempo("Horneado", bake, onBakeChange, editando) { onMicClick(CampoReceta.HORNEADO) }
    }
}

@Composable
private fun FilaTiempo(
    etiqueta: String,
    valor: String,
    onChange: (String) -> Unit,
    editando: Boolean,
    onMic: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, fontSize = 18.sp, modifier = Modifier.width(120.dp), color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = valor,
            onValueChange = { onChange(it.filter(Char::isDigit).take(4)) },
            readOnly = !editando,
            singleLine = true,
            placeholder = { Text("—") },
            suffix = { Text("min") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        if (editando) {
            IconButton(onClick = onMic, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Mic, contentDescription = "Dictar $etiqueta", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
