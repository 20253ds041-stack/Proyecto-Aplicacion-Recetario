package mx.edu.utez.mirecetario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        }

        Box(
            modifier = Modifier
                .width(200.dp)
                .padding(horizontal = 30.dp)
            ,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
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
                onValueChange = onDificultadChange,
                enabled = habilitado,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF5476AA),
                    disabledThumbColor = Color(0xFF5476AA),
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    disabledActiveTrackColor = Color.Transparent,
                    disabledInactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.width(200.dp)
            )
        }
    }
}