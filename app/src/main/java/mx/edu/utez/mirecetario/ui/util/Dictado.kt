package mx.edu.utez.mirecetario.ui.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import mx.edu.utez.mirecetario.ui.components.Alerta

/**
 * Dictado por campo con RecognizerIntent (reconocedor del sistema, español de México).
 * El reconocedor del sistema abre su propia interfaz y solo escucha mientras dura el dictado,
 * por lo que la app no necesita el permiso RECORD_AUDIO. Devuelve la función que inicia el dictado de un campo.
 * También muestra la confirmación cuando el dictado reemplazaría un valor ya escrito.
 */
@Composable
fun rememberDictadoReceta(form: RecetaFormState): (CampoReceta) -> Unit {
    val context = LocalContext.current
    var campoActivo by rememberSaveable { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val campo = campoActivo?.let { runCatching { CampoReceta.valueOf(it) }.getOrNull() }
        campoActivo = null
        if (result.resultCode == Activity.RESULT_OK) {
            val texto = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (texto.isNullOrBlank() || campo == null) {
                Toast.makeText(context, "No se reconoció ningún texto", Toast.LENGTH_SHORT).show()
            } else {
                form.aplicarDictado(campo, texto)?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
            }
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
            Toast.makeText(context, "No se pudo completar el dictado", Toast.LENGTH_SHORT).show()
        }
    }

    form.pendiente?.let { (campo, valor) ->
        Alerta(
            titulo = "¿Reemplazar ${campo.name.lowercase()} por \"$valor\"?",
            subtitulo = "Ya hay un valor escrito en este campo.",
            textoConfirmar = "Reemplazar",
            onConfirmar = { form.confirmarPendiente() },
            onCancelar = { form.pendiente = null }
        )
    }

    return { campo ->
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_PROMPT, campo.prompt)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        campoActivo = campo.name
        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            campoActivo = null
            Toast.makeText(context, "El reconocimiento de voz no está disponible en este dispositivo", Toast.LENGTH_LONG).show()
        }
    }
}
