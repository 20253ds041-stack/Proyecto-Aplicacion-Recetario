package mx.edu.utez.mirecetario.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InputsRecetas(
    tituloSeccion: String,
    placeholderTexto: String,
    items: List<String>,
    onItemChange: (Int, String) -> Unit,
    onAgregarItem: () -> Unit,
    modifier: Modifier = Modifier,
    editando: Boolean = true,
    onMicClick: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = tituloSeccion, fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)

            if (editando) {
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Hablar",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        items.forEachIndexed { index, textoActual ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}.",
                    fontSize = 20.sp,
                    modifier = Modifier.width(32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = textoActual,
                    onValueChange = { nuevoTexto -> onItemChange(index, nuevoTexto) },
                    readOnly = !editando,
                    singleLine = true,
                    placeholder = { Text(placeholderTexto) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (editando) {
            IconButton(
                onClick = onAgregarItem,
                modifier = Modifier
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddBox,
                    contentDescription = "Agregar",
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
