package mx.edu.utez.mirecetario.data.local

import com.google.gson.Gson
import com.google.gson.JsonParseException
import mx.edu.utez.mirecetario.data.remote.RecipeRequest
import mx.edu.utez.mirecetario.domain.model.Recipe
import mx.edu.utez.mirecetario.domain.model.RecipeInput
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Formato de intercambio: ZIP con recipe.json + images/image_N.jpg (rutas siempre relativas). */
data class ExportedRecipe(
    val schemaVersion: Int? = null,
    val title: String? = null,
    val difficulty: Int? = null,
    val prepMinutes: Int? = null,
    val cookMinutes: Int? = null,
    val bakeMinutes: Int? = null,
    val ingredients: List<String>? = null,
    val instructions: List<String>? = null,
    val images: List<String>? = null
)

class ImportedRecipe(val input: RecipeInput, val images: List<ByteArray>)

object RecipeZip {
    const val SCHEMA_VERSION = 1
    const val JSON_NAME = "recipe.json"
    private const val MAX_ENTRIES = 50
    /** Límites compartidos con el servidor y el cliente: un ZIP exportado por la app siempre cabe aquí. */
    const val MAX_ZIP_BYTES = 40 * 1024 * 1024
    const val MAX_IMAGE_ENTRY_BYTES = 25 * 1024 * 1024
    private const val MAX_TOTAL_BYTES = 60L * 1024 * 1024
    const val MAX_IMAGES = 12
    private val gson = Gson()

    fun export(recipe: Recipe, readImage: (String) -> ByteArray?): ByteArray {
        val names = mutableListOf<String>()
        val files = mutableListOf<ByteArray>()
        recipe.photos.take(MAX_IMAGES).forEach { photo ->
            readImage(photo.path)?.let {
                names += "images/image_${names.size + 1}.jpg"
                files += it
            }
        }
        val json = ExportedRecipe(
            SCHEMA_VERSION, recipe.title, recipe.difficulty, recipe.prepMinutes, recipe.cookMinutes,
            recipe.bakeMinutes, recipe.ingredients, recipe.instructions, names
        )
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(JSON_NAME))
            zip.write(gson.toJson(json).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            names.forEachIndexed { i, name ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(files[i])
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    /** Lee y valida el ZIP completo en memoria. Lanza IllegalArgumentException si es inválido. */
    fun parse(zipBytes: ByteArray): ImportedRecipe {
        val entries = readEntries(zipBytes)
        val jsonBytes = entries[JSON_NAME] ?: throw IllegalArgumentException("El ZIP no contiene $JSON_NAME")
        val exported = try {
            gson.fromJson(String(jsonBytes, Charsets.UTF_8), ExportedRecipe::class.java)
        } catch (e: JsonParseException) {
            null
        } ?: throw IllegalArgumentException("$JSON_NAME no es un JSON válido")

        val version = exported.schemaVersion ?: throw IllegalArgumentException("Falta schemaVersion")
        require(version in 1..SCHEMA_VERSION) { "Versión de formato no soportada: $version" }

        val input = RecipeValidator.validate(
            RecipeRequest(
                exported.title, exported.difficulty, exported.prepMinutes, exported.cookMinutes,
                exported.bakeMinutes, exported.ingredients, exported.instructions
            )
        )
        val refs = exported.images.orEmpty().filterNotNull()
        require(refs.size <= MAX_IMAGES) { "Demasiadas imágenes (máx. $MAX_IMAGES)" }
        val images = refs.map { ref ->
            require(isSafeName(ref) && ref.startsWith("images/")) { "Referencia de imagen inválida: $ref" }
            entries[ref] ?: throw IllegalArgumentException("Falta la imagen $ref en el ZIP")
        }
        return ImportedRecipe(input, images)
    }

    private fun readEntries(zipBytes: ByteArray): Map<String, ByteArray> {
        val result = LinkedHashMap<String, ByteArray>()
        var total = 0L
        var entryCount = 0
        try {
            ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    require(++entryCount <= MAX_ENTRIES) { "El ZIP contiene demasiados archivos" }
                    if (!entry.isDirectory) {
                        require(isSafeName(entry.name)) { "El ZIP contiene rutas no permitidas" }
                        require(!result.containsKey(entry.name)) { "El ZIP contiene entradas duplicadas: ${entry.name}" }
                        var entryBytes = 0L
                        val buffer = ByteArrayOutputStream()
                        val chunk = ByteArray(8192)
                        while (true) {
                            val n = zis.read(chunk)
                            if (n < 0) break
                            total += n
                            entryBytes += n
                            require(total <= MAX_TOTAL_BYTES) { "El ZIP es demasiado grande" }
                            require(entryBytes <= MAX_IMAGE_ENTRY_BYTES) { "Un archivo del ZIP es demasiado grande" }
                            buffer.write(chunk, 0, n)
                        }
                        result[entry.name] = buffer.toByteArray()
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (e: java.io.IOException) {
            throw IllegalArgumentException("El archivo ZIP está dañado")
        }
        require(result.isNotEmpty()) { "El archivo ZIP está vacío o dañado" }
        return result
    }

    /** Solo rutas relativas simples: sin "..", sin barras iniciales ni invertidas. */
    fun isSafeName(name: String): Boolean =
        name.isNotEmpty() && !name.startsWith("/") && !name.contains('\\') && !name.contains('\u0000') &&
            name.split('/').none { it == ".." || it.isEmpty() }
}
