package mx.edu.utez.mirecetario.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun RecetasGuardadas(
    onCrearClick: () -> Unit,
    onInfoClick: ()-> Unit
) {
    Column {
        Text("Recetas Guardadas")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { },
                singleLine = true,
                label = {},
                modifier = Modifier
                    .width(200.dp)
                    .height(40.dp)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Button(
                onClick = onCrearClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5476AA),
                    contentColor = Color.White
                )
            ) {
                Row {
                    Text("Crear")
                    Icon(
                        imageVector = Icons.Default.AddBox,
                        contentDescription = "Anadir receta"
                    )
                }
            }

        }
        Button(
            onClick = onInfoClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5476AA),
                contentColor = Color.White
            )
        ){}
    }
}