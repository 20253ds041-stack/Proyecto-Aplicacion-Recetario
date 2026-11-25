package mx.edu.utez.mirecetario.domain.model

data class User(val id: Long, val name: String, val email: String, val avatarPath: String? = null)

data class Photo(val id: Long, val path: String, val position: Int)

data class Recipe(
    val id: Long,
    val userId: Long,
    val title: String,
    val difficulty: Int,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val bakeMinutes: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val photos: List<Photo>,
    val updatedAt: Long
)

/** Datos editables de una receta (ya validados). */
data class RecipeInput(
    val title: String,
    val difficulty: Int,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val bakeMinutes: Int,
    val ingredients: List<String>,
    val instructions: List<String>
)
