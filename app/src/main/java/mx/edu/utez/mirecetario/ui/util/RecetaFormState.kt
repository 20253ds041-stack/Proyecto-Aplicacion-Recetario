package mx.edu.utez.mirecetario.ui.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import mx.edu.utez.mirecetario.data.remote.RecipeDto
import mx.edu.utez.mirecetario.data.remote.RecipeRequest

enum class CampoReceta(val prompt: String) {
    TITULO("Di el título de la receta"),
    DIFICULTAD("Di la dificultad: fácil, media o difícil"),
    PREPARACION("Di el tiempo de preparación, por ejemplo: 20 minutos"),
    COCCION("Di el tiempo de cocción, por ejemplo: 30 minutos"),
    HORNEADO("Di el tiempo de horneado, por ejemplo: una hora"),
    INGREDIENTES("Di un ingrediente"),
    INSTRUCCIONES("Di un paso de la receta")
}

/** Estado editable del formulario de receta (compartido por crear y editar). */
class RecetaFormState {
    var titulo by mutableStateOf("")
    var dificultad by mutableFloatStateOf(0.5f)
    var prep by mutableStateOf("")
    var cook by mutableStateOf("")
    var bake by mutableStateOf("")
    val ingredientes = mutableStateListOf("")
    val instrucciones = mutableStateListOf("")

    /** Dictado que esperaría confirmación porque reemplazaría un valor existente. */
    var pendiente by mutableStateOf<Pair<CampoReceta, String>?>(null)

    val nivel: Int get() = if (dificultad < 0.34f) 1 else if (dificultad < 0.67f) 2 else 3

    fun cargar(r: RecipeDto) {
        titulo = r.title
        dificultad = nivelAFloat(r.difficulty)
        prep = r.prepMinutes.takeIf { it > 0 }?.toString().orEmpty()
        cook = r.cookMinutes.takeIf { it > 0 }?.toString().orEmpty()
        bake = r.bakeMinutes.takeIf { it > 0 }?.toString().orEmpty()
        ingredientes.clear(); ingredientes.addAll(r.ingredients.ifEmpty { listOf("") })
        instrucciones.clear(); instrucciones.addAll(r.instructions.ifEmpty { listOf("") })
    }

    fun validar(): String? = if (titulo.isBlank()) "Escribe el título de la receta" else null

    fun toRequest() = RecipeRequest(
        title = titulo.trim(),
        difficulty = nivel,
        prepMinutes = prep.toIntOrNull() ?: 0,
        cookMinutes = cook.toIntOrNull() ?: 0,
        bakeMinutes = bake.toIntOrNull() ?: 0,
        ingredients = ingredientes.map { it.trim() }.filter { it.isNotEmpty() },
        instructions = instrucciones.map { it.trim() }.filter { it.isNotEmpty() }
    )

    /** Coloca el texto dictado en su campo. Devuelve un aviso para el usuario si no se pudo interpretar. */
    fun aplicarDictado(campo: CampoReceta, texto: String): String? {
        val limpio = texto.trim()
        when (campo) {
            CampoReceta.TITULO -> escribir(campo, DictadoParser.capitalizar(limpio), titulo)
            CampoReceta.DIFICULTAD -> {
                val n = DictadoParser.dificultad(limpio) ?: return "No entendí la dificultad. Di fácil, media o difícil"
                dificultad = nivelAFloat(n)
                return "Dificultad: ${nombreNivel(n)}"
            }
            CampoReceta.PREPARACION, CampoReceta.COCCION, CampoReceta.HORNEADO -> {
                val m = DictadoParser.minutos(limpio) ?: return "No entendí el tiempo. Ejemplo: 30 minutos"
                escribir(campo, m.toString(), valorTiempo(campo))
            }
            CampoReceta.INGREDIENTES -> agregarItem(ingredientes, DictadoParser.capitalizar(limpio))
            CampoReceta.INSTRUCCIONES -> agregarItem(instrucciones, DictadoParser.capitalizar(limpio))
        }
        return null
    }

    fun confirmarPendiente() {
        pendiente?.let { (campo, valor) -> asignar(campo, valor) }
        pendiente = null
    }

    private fun escribir(campo: CampoReceta, nuevo: String, actual: String) {
        if (actual.isBlank()) asignar(campo, nuevo) else pendiente = campo to nuevo
    }

    private fun asignar(campo: CampoReceta, valor: String) {
        when (campo) {
            CampoReceta.TITULO -> titulo = valor
            CampoReceta.PREPARACION -> prep = valor
            CampoReceta.COCCION -> cook = valor
            CampoReceta.HORNEADO -> bake = valor
            else -> Unit
        }
    }

    private fun valorTiempo(campo: CampoReceta) = when (campo) {
        CampoReceta.PREPARACION -> prep
        CampoReceta.COCCION -> cook
        else -> bake
    }

    private fun agregarItem(lista: MutableList<String>, texto: String) {
        if (lista.isNotEmpty() && lista.last().isBlank()) lista[lista.lastIndex] = texto else lista.add(texto)
    }

    companion object {
        fun nivelAFloat(nivel: Int) = (nivel.coerceIn(1, 3) - 1) / 2f
        fun nombreNivel(nivel: Int) = when (nivel) { 1 -> "fácil"; 3 -> "difícil"; else -> "media" }
    }
}
