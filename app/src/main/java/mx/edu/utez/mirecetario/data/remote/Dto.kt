package mx.edu.utez.mirecetario.data.remote

import mx.edu.utez.mirecetario.domain.model.Recipe

// Los campos de las peticiones son anulables: Gson no respeta los tipos no nulos de Kotlin
// y el servidor valida explícitamente cada uno.
data class RegisterRequest(val name: String? = null, val email: String? = null, val password: String? = null)
data class LoginRequest(val email: String? = null, val password: String? = null)
data class RecipeRequest(
    val title: String? = null,
    val difficulty: Int? = null,
    val prepMinutes: Int? = null,
    val cookMinutes: Int? = null,
    val bakeMinutes: Int? = null,
    val ingredients: List<String>? = null,
    val instructions: List<String>? = null
)

data class UserDto(val id: Long, val name: String, val email: String, val avatarPath: String? = null)
data class AuthResponse(val token: String, val user: UserDto)
data class PhotoDto(val id: Long, val path: String)
data class RecipeDto(
    val id: Long,
    val title: String,
    val difficulty: Int,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val bakeMinutes: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val photos: List<PhotoDto>,
    val updatedAt: Long
) {
    val totalMinutes: Int get() = prepMinutes + cookMinutes + bakeMinutes
}
data class ErrorDto(val error: String)

fun Recipe.toDto() = RecipeDto(
    id, title, difficulty, prepMinutes, cookMinutes, bakeMinutes,
    ingredients, instructions, photos.map { PhotoDto(it.id, it.path) }, updatedAt
)
