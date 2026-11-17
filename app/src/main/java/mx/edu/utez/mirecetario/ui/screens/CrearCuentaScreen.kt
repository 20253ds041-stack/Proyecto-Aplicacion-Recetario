package mx.edu.utez.mirecetario.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.scrollAway
import mx.edu.utez.mirecetario.ui.theme.MiRecetarioTheme

@Composable
fun CrearCuentaScreen(
    modifier : Modifier = Modifier,
    onCrearCuentaClick: (name: String, email: String, password: String )-> Unit
) {
    //variables mientras no hay viewModel
    var name by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Crear cuenta",
            fontSize = 35.sp,
            color = Color(0xFF5476AA),
        )

        Spacer(modifier=Modifier.height(35.dp))

        Text(
            text = "Nombre",
            fontSize = 28.sp,
            color = Color(0xFF5476AA)
        )
        OutlinedTextField(
            value = "nombre",
            onValueChange = { name= it},
            readOnly = true,
            singleLine = true,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(modifier= Modifier.height(20.dp))

        Text(
            text = "Correo electrónico",
            fontSize = 28.sp,
            color = Color(0xFF5476AA)
        )
        OutlinedTextField(
            value = "samantha@gmail.com",
            onValueChange = {email= it},
            readOnly = true,
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier= Modifier.fillMaxWidth()
        )

        Spacer(modifier= Modifier.height(20.dp))

        Text(
            text = "Contraseña",
            fontSize = 28.sp,
            color = Color(0xFF5476AA)
        )
        OutlinedTextField(
            value = "......",
            onValueChange = {password=it},
            readOnly = true,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier= Modifier.fillMaxWidth()
        )

        Spacer(modifier= Modifier.height(32.dp))

        //botón para crear cuenta
        Button(
            onClick = { onCrearCuentaClick(name, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Crear")
        }


    }
}

@Preview(showBackground = true)
@Composable
fun CrearCuentaPreview() {
    MiRecetarioTheme() {
        CrearCuentaScreen( onCrearCuentaClick = { _, _, _ -> })
    }
}