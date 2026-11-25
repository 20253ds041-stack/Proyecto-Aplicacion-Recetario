package mx.edu.utez.mirecetario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarraDificultadComponent(
    dificultad: Float,
    onDificultadChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val nivel = if (dificultad < 0.34f) 1 else if (dificultad < 0.67f) 2 else 3
    val (textoDificultad, colorDificultad) = when (nivel) {
        1 -> "Fácil" to Color(0xFF2E7D32)     // Verde
        2 -> "Media" to Color(0xFFFFEB3B)    // Amarillo
        else -> "Difícil" to Color(0xFFC62828) // Rojo
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón toggle a la izquierda para definir la dificultad
        Surface(
            onClick = {
                if (habilitado) {
                    val siguienteNivel = if (nivel == 1) 2 else if (nivel == 2) 3 else 1
                    val nuevoValor = when (siguienteNivel) {
                        1 -> 0.0f
                        2 -> 0.5f
                        else -> 1.0f
                    }
                    onDificultadChange(nuevoValor)
                }
            },
            enabled = habilitado,
            shape = RoundedCornerShape(12.dp),
            color = colorDificultad.copy(alpha = 0.15f),
            contentColor = colorDificultad,
            modifier = Modifier.height(44.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$textoDificultad",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorDificultad
                )
            }
        }

        // Slider decorativo a la derecha
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFFFFEB3B),
                                Color(0xFFF44336)
                            )
                        )
                    )
            )

            Slider(
                value = dificultad,
                onValueChange = {},
                enabled = false,
                colors = SliderDefaults.colors(
                    thumbColor = colorDificultad,
                    disabledThumbColor = colorDificultad,
                    disabledActiveTrackColor = Color.Transparent,
                    disabledInactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

