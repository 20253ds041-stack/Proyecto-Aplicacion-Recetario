package mx.edu.utez.mirecetario

import mx.edu.utez.mirecetario.data.local.PasswordHasher
import mx.edu.utez.mirecetario.data.local.RecipeValidator
import mx.edu.utez.mirecetario.data.local.RecipeZip
import mx.edu.utez.mirecetario.data.remote.RecipeRequest
import mx.edu.utez.mirecetario.domain.model.Photo
import mx.edu.utez.mirecetario.domain.model.Recipe
import mx.edu.utez.mirecetario.ui.util.DictadoParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RecetarioLogicTest {

    @Test fun hash_verifica_contrasena_correcta_e_incorrecta() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("secreto1", salt)
        assertTrue(PasswordHasher.verify("secreto1", salt, hash))
        assertFalse(PasswordHasher.verify("otra", salt, hash))
        assertFalse(hash.contains("secreto1"))
    }

    @Test fun dictado_dificultad() {
        assertEquals(1, DictadoParser.dificultad("es fácil"))
        assertEquals(2, DictadoParser.dificultad("Media"))
        assertEquals(3, DictadoParser.dificultad("difícil"))
        assertNull(DictadoParser.dificultad("hola"))
    }

    @Test fun dictado_minutos() {
        assertEquals(30, DictadoParser.minutos("30 minutos"))
        assertEquals(45, DictadoParser.minutos("cuarenta y cinco minutos"))
        assertEquals(60, DictadoParser.minutos("una hora"))
        assertEquals(90, DictadoParser.minutos("1 hora y media"))
        assertEquals(30, DictadoParser.minutos("media hora"))
        assertEquals(150, DictadoParser.minutos("2 horas 30 minutos"))
        assertEquals(90, DictadoParser.minutos("1 hora 30"))
        assertEquals(120, DictadoParser.minutos("2 horas"))
        assertEquals(20, DictadoParser.minutos("20"))
        assertNull(DictadoParser.minutos("nada"))
    }

    @Test fun validador_rechaza_titulo_vacio_y_normaliza() {
        try {
            RecipeValidator.validate(RecipeRequest(title = "  "))
            fail("Debía fallar")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("título"))
        }
        val ok = RecipeValidator.validate(RecipeRequest(title = " Pan ", ingredients = listOf(" harina ", "", " ")))
        assertEquals("Pan", ok.title)
        assertEquals(2, ok.difficulty)
        assertEquals(listOf("harina"), ok.ingredients)
    }

    @Test fun zip_exporta_e_importa_receta_con_imagenes() {
        val recipe = Recipe(
            7, 3, "Sopa", 1, 10, 20, 0, listOf("Agua", "Sal"), listOf("Hervir"),
            listOf(Photo(1, "images/7/a.jpg", 0), Photo(2, "images/7/b.jpg", 1)), 0L
        )
        val zip = RecipeZip.export(recipe) { path -> path.toByteArray() }
        val imported = RecipeZip.parse(zip)
        assertEquals("Sopa", imported.input.title)
        assertEquals(listOf("Agua", "Sal"), imported.input.ingredients)
        assertEquals(20, imported.input.cookMinutes)
        assertEquals(2, imported.images.size)
        assertEquals("images/7/a.jpg", String(imported.images[0]))
    }

    @Test fun zip_rechaza_rutas_peligrosas_y_archivos_invalidos() {
        assertFalse(RecipeZip.isSafeName("../x.jpg"))
        assertFalse(RecipeZip.isSafeName("/etc/passwd"))
        assertFalse(RecipeZip.isSafeName("images\\x.jpg"))
        assertTrue(RecipeZip.isSafeName("images/image_1.jpg"))

        val traversal = ByteArrayOutputStream().also { out ->
            ZipOutputStream(out).use { z ->
                z.putNextEntry(ZipEntry("../evil.txt")); z.write(1); z.closeEntry()
            }
        }.toByteArray()
        assertThrows { RecipeZip.parse(traversal) }
        assertThrows { RecipeZip.parse("no es un zip".toByteArray()) }

        val sinJson = ByteArrayOutputStream().also { out ->
            ZipOutputStream(out).use { z -> z.putNextEntry(ZipEntry("otro.txt")); z.write(1); z.closeEntry() }
        }.toByteArray()
        assertThrows { RecipeZip.parse(sinJson) }
    }

    @Test fun zip_rechaza_entradas_duplicadas() {
        // ZipOutputStream no admite nombres repetidos: se crean dos nombres de igual longitud y se
        // renombra el segundo en los bytes (ZipInputStream lee solo las cabeceras locales).
        val original = ByteArrayOutputStream().also { out ->
            ZipOutputStream(out).use { z ->
                z.putNextEntry(ZipEntry("recipe.jsoa")); z.write("{}".toByteArray()); z.closeEntry()
                z.putNextEntry(ZipEntry("recipe.json")); z.write("{}".toByteArray()); z.closeEntry()
            }
        }.toByteArray()
        val duplicado = String(original, Charsets.ISO_8859_1).replace("recipe.jsoa", "recipe.json")
            .toByteArray(Charsets.ISO_8859_1)
        try {
            RecipeZip.parse(duplicado)
            fail("Debía rechazar entradas duplicadas")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("duplicadas"))
        }
    }

    @Test fun zip_exportado_se_puede_importar_dentro_de_los_limites() {
        val fotos = (1..RecipeZip.MAX_IMAGES + 3).map { Photo(it.toLong(), "images/1/$it.jpg", it) }
        val receta = Recipe(1, 1, "Muchas fotos", 2, 0, 0, 0, emptyList(), emptyList(), fotos, 0L)
        val zip = RecipeZip.export(receta) { ByteArray(1000) }
        assertTrue(zip.size < RecipeZip.MAX_ZIP_BYTES)
        assertEquals(RecipeZip.MAX_IMAGES, RecipeZip.parse(zip).images.size)
    }

    private fun assertThrows(block: () -> Unit) {
        try {
            block()
            fail("Debía lanzar IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // esperado
        }
    }
}
