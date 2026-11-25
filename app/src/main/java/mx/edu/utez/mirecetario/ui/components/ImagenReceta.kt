package mx.edu.utez.mirecetario.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TARGET_PX = 1080

private fun sampleFor(width: Int, height: Int): Int {
    var sample = 1
    while (maxOf(width, height) / (sample * 2) >= TARGET_PX) sample *= 2
    return sample
}

/** Foto guardada en el almacenamiento privado (ruta relativa a filesDir). */
@Composable
fun ImagenReceta(
    rutaRelativa: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(null, rutaRelativa) {
        value = withContext(Dispatchers.IO) {
            val file = File(context.filesDir, rutaRelativa)
            if (!file.isFile) return@withContext null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply {
                inSampleSize = sampleFor(bounds.outWidth, bounds.outHeight)
            })?.asImageBitmap()
        }
    }
    val b = bitmap
    if (b != null) Image(bitmap = b, contentDescription = "Fotografía de la receta", modifier = modifier, contentScale = contentScale)
}

/** Vista previa de una foto elegida en el selector, antes de guardarla. */
@Composable
fun ImagenUri(uri: Uri, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) { decodificarUri(context, uri) }
    }
    val b = bitmap
    if (b != null) Image(bitmap = b, contentDescription = "Fotografía seleccionada", modifier = modifier, contentScale = contentScale)
}

private fun decodificarUri(context: Context, uri: Uri): ImageBitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply {
            inSampleSize = sampleFor(bounds.outWidth, bounds.outHeight)
        })
    }?.asImageBitmap()
} catch (e: Exception) {
    null
}
