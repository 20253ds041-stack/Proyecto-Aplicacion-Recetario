package mx.edu.utez.mirecetario.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.mirecetario.R
import mx.edu.utez.mirecetario.ui.components.Alerta
import mx.edu.utez.mirecetario.ui.components.BarraNavegacionComponent
import mx.edu.utez.mirecetario.ui.theme.MiRecetarioTheme

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    onIniciarSesion: ()-> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var cerrar by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = "Perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(40.dp))
            Text(
                text = "Nombre", fontSize = 28.sp,color = Color(0xFF5476AA)
            )
        }
        Spacer(modifier = Modifier.height(30.dp))

        // Input de correo
        Text("Correo electrónico", fontSize = 28.sp,color = Color(0xFF5476AA))
        OutlinedTextField(
            value = "usuario@ejemplo.com",
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            label = { },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(30.dp))

        // Input de contrasena
        Text("Contrasena", fontSize = 28.sp,color = Color(0xFF5476AA))
        OutlinedTextField(
            value = "contrasena",
            onValueChange = { },
            readOnly = true,
            label = { },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icono =
                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val descripcion =
                    if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icono, contentDescription = descripcion)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(30.dp))

        // btn cerrar sesion
        Button(
            onClick = {cerrar=true},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5476AA),
                contentColor = Color.White
            )
        ) {
            Text("Cerrar sesion")
        }
        if (cerrar){
            Alerta(
                titulo = "¿Estás seguro de cerrar sesión?",
                textoConfirmar = "Salir",
                onConfirmar = {
                    cerrar=false
                   onIniciarSesion()
                },
                onCancelar = {
                    cerrar=false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilScreenPreview() {
    MiRecetarioTheme {
        PerfilScreen(modifier = Modifier.padding(0.dp),{})
    }
}

