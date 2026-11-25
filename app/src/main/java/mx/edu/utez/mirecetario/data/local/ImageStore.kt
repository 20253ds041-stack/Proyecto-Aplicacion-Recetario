package mx.edu.utez.mirecetario.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/**
 * Guarda las fotografías en el almacenamiento privado de la app (filesDir/images/{recetaId}/).
 * Cada imagen se reduce a [MAX_SIDE] px y se comprime a JPEG antes de escribirse.
 * En SQLite solo se registra la ruta relativa (p. ej. "images/12/ab34.jpg").
 */
class ImageStore(private val root: File) {

    /** Comprime y guarda; devuelve la ruta relativa. Lanza IllegalArgumentException si no es una imagen. */
    fun save(recipeId: Long, original: ByteArray): String {
        val jpeg = compress(original)
        val rel = "$DIR/$recipeId/${UUID.randomUUID()}.jpg"
        val file = File(root, rel)
        file.parentFile?.mkdirs()
        file.writeBytes(jpeg)
        return rel
    }

    fun saveUserAvatar(userId: Long, original: ByteArray): String {
        val jpeg = compress(original)
        val rel = "$DIR/users/$userId/${UUID.randomUUID()}.jpg"
        val file = File(root, rel)
        file.parentFile?.mkdirs()
        file.writeBytes(jpeg)
        return rel
    }

    fun read(relativePath: String): ByteArray? = resolve(relativePath)?.takeIf { it.isFile }?.readBytes()

    fun delete(relativePath: String) {
        resolve(relativePath)?.delete()
    }

    fun deleteRecipeDir(recipeId: Long) {
        File(root, "$DIR/$recipeId").deleteRecursively()
    }

    /** Borra archivos de imagen que ya no están registrados en SQLite (p. ej. tras un cierre inesperado). */
    fun deleteOrphans(registeredPaths: Set<String>) {
        val base = File(root, DIR)
        base.listFiles()?.forEach { dir ->
            if (!dir.isDirectory) return@forEach
            dir.listFiles()?.forEach { file ->
                if ("$DIR/${dir.name}/${file.name}" !in registeredPaths) file.delete()
            }
            if (dir.list().isNullOrEmpty()) dir.delete()
        }
    }

    /** Resuelve la ruta relativa sin permitir salir de la carpeta de imágenes. */
    private fun resolve(relativePath: String): File? {
        val base = File(root, DIR).canonicalFile
        val file = File(root, relativePath).canonicalFile
        return if (file.path.startsWith(base.path + File.separator)) file else null
    }

    private fun compress(bytes: ByteArray): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "El archivo no es una imagen válida" }

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) sample *= 2
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: throw IllegalArgumentException("No se pudo leer la imagen")

        val matrix = Matrix()
        val scale = min(1f, MAX_SIDE.toFloat() / max(decoded.width, decoded.height))
        if (scale < 1f) matrix.postScale(scale, scale)
        val rotation = exifRotation(bytes)
        if (rotation != 0) matrix.postRotate(rotation.toFloat())
        var result = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (result !== decoded) decoded.recycle()

        if (result.hasAlpha()) { // JPEG no admite transparencia: fondo blanco
            val flat = Bitmap.createBitmap(result.width, result.height, Bitmap.Config.RGB_565)
            Canvas(flat).apply { drawColor(Color.WHITE); drawBitmap(result, 0f, 0f, null) }
            result.recycle()
            result = flat
        }
        val out = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        result.recycle()
        return out.toByteArray()
    }

    private fun exifRotation(bytes: ByteArray): Int = try {
        when (ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: Exception) {
        0
    }

    companion object {
        const val DIR = "images"
        const val MAX_SIDE = 1280
        const val QUALITY = 82
    }
}
