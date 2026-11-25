package mx.edu.utez.mirecetario.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.mirecetario.ui.theme.MiRecetarioTheme

@Composable
fun CrearCuentaScreen(
    modifier: Modifier = Modifier,
    onCrearCuentaClick: (name: String, email: String, password: String) -> Unit,
    onVolverClick: () -> Unit = {},
    cargando: Boolean = false,
    error: String? = null
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Crear cuenta",
            fontSize = 35.sp,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "Nombre",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Correo electrónico",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Contraseña",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña (mín. 6 caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = error, color = Color(0xFFC62828), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onCrearCuentaClick(name, email, password) },
            enabled = !cargando,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = if (cargando) "Creando..." else "Crear")
        }

        TextButton(onClick = onVolverClick) {
            Text(text = "Ya tengo cuenta", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrearCuentaPreview() {
    MiRecetarioTheme {
        CrearCuentaScreen(onCrearCuentaClick = { _, _, _ -> })
    }
}
