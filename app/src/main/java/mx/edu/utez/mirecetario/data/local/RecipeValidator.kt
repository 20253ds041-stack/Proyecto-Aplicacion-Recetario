package mx.edu.utez.mirecetario.data.local

import mx.edu.utez.mirecetario.data.remote.RecipeRequest
import mx.edu.utez.mirecetario.domain.model.RecipeInput

/** Valida y normaliza los datos de una receta. Lanza IllegalArgumentException con mensaje legible. */
object RecipeValidator {
    private const val MAX_MINUTES = 10_080 // una semana
    private const val MAX_ITEMS = 100
    private const val MAX_ITEM_LENGTH = 500

    fun validate(r: RecipeRequest): RecipeInput {
        val title = r.title?.trim().orEmpty()
        require(title.isNotEmpty()) { "El título es obligatorio" }
        require(title.length <= 120) { "El título es demasiado largo (máx. 120)" }
        val difficulty = r.difficulty ?: 2
        require(difficulty in 1..3) { "La dificultad debe estar entre 1 y 3" }
        return RecipeInput(
            title = title,
            difficulty = difficulty,
            prepMinutes = minutes(r.prepMinutes, "preparación"),
            cookMinutes = minutes(r.cookMinutes, "cocción"),
            bakeMinutes = minutes(r.bakeMinutes, "horneado"),
            ingredients = items(r.ingredients, "ingredientes"),
            instructions = items(r.instructions, "instrucciones")
        )
    }

    private fun minutes(value: Int?, label: String): Int {
        val v = value ?: 0
        require(v in 0..MAX_MINUTES) { "El tiempo de $label debe estar entre 0 y $MAX_MINUTES minutos" }
        return v
    }

    private fun items(list: List<String?>?, label: String): List<String> {
        val clean = list.orEmpty().mapNotNull { it?.trim() }.filter { it.isNotEmpty() }
        require(clean.size <= MAX_ITEMS) { "Demasiados $label (máx. $MAX_ITEMS)" }
        require(clean.all { it.length <= MAX_ITEM_LENGTH }) { "Un elemento de $label es demasiado largo" }
        return clean
    }
}
