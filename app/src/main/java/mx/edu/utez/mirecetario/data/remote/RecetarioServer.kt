package mx.edu.utez.mirecetario.data.remote

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import mx.edu.utez.mirecetario.data.local.ImageStore
import mx.edu.utez.mirecetario.data.local.PasswordHasher
import mx.edu.utez.mirecetario.data.local.RecetarioDao
import mx.edu.utez.mirecetario.data.local.RecipeValidator
import mx.edu.utez.mirecetario.data.local.RecipeZip
import mx.edu.utez.mirecetario.domain.model.Recipe
import java.io.ByteArrayOutputStream

class ApiException(val status: HttpStatusCode, message: String) : RuntimeException(message)

private const val MAX_IMAGE_BYTES = 25 * 1024 * 1024
private const val MAX_IMPORT_BYTES = RecipeZip.MAX_ZIP_BYTES
private const val MAX_PHOTOS_PER_RECIPE = RecipeZip.MAX_IMAGES
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** API REST local. Solo delega en [RecetarioDao] / [ImageStore]; escucha únicamente en 127.0.0.1. */
fun Application.recetarioModule(dao: RecetarioDao, images: ImageStore) {
    install(ContentNegotiation) { gson() }
    install(StatusPages) {
        exception<ApiException> { call, e -> call.respond(e.status, ErrorDto(e.message ?: "Error")) }
        exception<IllegalArgumentException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ErrorDto(e.message ?: "Datos inválidos"))
        }
        // Solo Exception: los Error fatales (p. ej. OutOfMemoryError) no se disfrazan de respuesta 500.
        exception<Exception> { call, e ->
            if (e is CancellationException) throw e
            call.respond(HttpStatusCode.InternalServerError, ErrorDto("Error interno del servidor"))
        }
    }

    routing {
        get("/health") { call.respond(mapOf("status" to "ok", "app" to "recetario")) }

        route("/auth") {
            post("/register") {
                val req = call.receiveJson<RegisterRequest>()
                val name = req.name?.trim().orEmpty()
                val email = req.email?.trim()?.lowercase().orEmpty()
                val password = req.password.orEmpty()
                require(name.isNotEmpty() && name.length <= 80) { "El nombre es obligatorio (máx. 80 caracteres)" }
                require(EMAIL_REGEX.matches(email) && email.length <= 120) { "El correo electrónico no es válido" }
                require(password.length in 6..128) { "La contraseña debe tener entre 6 y 128 caracteres" }
                val salt = PasswordHasher.newSalt()
                val id = dao.createUser(name, email, PasswordHasher.hash(password, salt), salt)
                    ?: throw ApiException(HttpStatusCode.Conflict, "Ya existe una cuenta con ese correo")
                val user = dao.getUser(id) ?: throw ApiException(HttpStatusCode.InternalServerError, "Error al crear la cuenta")
                call.respond(HttpStatusCode.Created, AuthResponse(dao.createSession(id), UserDto(user.id, user.name, user.email, user.avatarPath)))
            }
            post("/login") {
                dao.purgeExpiredSessions()
                val req = call.receiveJson<LoginRequest>()
                val email = req.email?.trim()?.lowercase().orEmpty()
                val password = req.password.orEmpty()
                require(email.isNotEmpty() && password.isNotEmpty()) { "Ingresa correo y contraseña" }
                val row = dao.findUserByEmail(email)
                if (row == null || !PasswordHasher.verify(password, row.salt, row.passwordHash)) {
                    throw ApiException(HttpStatusCode.Unauthorized, "Correo o contraseña incorrectos")
                }
                val u = row.user
                call.respond(AuthResponse(dao.createSession(u.id), UserDto(u.id, u.name, u.email, u.avatarPath)))
            }
            post("/logout") {
                call.bearerToken()?.let { dao.deleteSession(it) }
                call.respond(HttpStatusCode.NoContent)
            }
            get("/me") {
                val user = dao.getUser(call.requireUser(dao)) ?: throw ApiException(HttpStatusCode.Unauthorized, "Sesión inválida")
                call.respond(UserDto(user.id, user.name, user.email, user.avatarPath))
            }
            post("/avatar") {
                val uid = call.requireUser(dao)
                val bytes = call.receiveBytes(MAX_IMAGE_BYTES)
                val newPath = images.saveUserAvatar(uid, bytes)
                val user = dao.getUser(uid) ?: throw ApiException(HttpStatusCode.NotFound, "Usuario no encontrado")
                val oldPath = user.avatarPath
                val updated = dao.updateUserAvatar(uid, newPath)
                if (!updated) {
                    images.delete(newPath)
                    throw ApiException(HttpStatusCode.InternalServerError, "No se pudo actualizar la foto de perfil")
                }
                if (!oldPath.isNullOrBlank() && oldPath != newPath) {
                    runCatching { images.delete(oldPath) }
                }
                val updatedUser = dao.getUser(uid)!!
                call.respond(UserDto(updatedUser.id, updatedUser.name, updatedUser.email, updatedUser.avatarPath))
            }
            delete("/avatar") {
                val uid = call.requireUser(dao)
                val user = dao.getUser(uid) ?: throw ApiException(HttpStatusCode.NotFound, "Usuario no encontrado")
                val oldPath = user.avatarPath
                dao.updateUserAvatar(uid, null)
                if (!oldPath.isNullOrBlank()) {
                    runCatching { images.delete(oldPath) }
                }
                val updatedUser = dao.getUser(uid)!!
                call.respond(UserDto(updatedUser.id, updatedUser.name, updatedUser.email, updatedUser.avatarPath))
            }
        }

        route("/recipes") {
            get {
                val uid = call.requireUser(dao)
                call.respond(dao.listRecipes(uid).map { it.toDto() })
            }
            post {
                val uid = call.requireUser(dao)
                val input = RecipeValidator.validate(call.receiveJson<RecipeRequest>())
                val id = dao.insertRecipe(uid, input)
                call.respond(HttpStatusCode.Created, dao.getRecipe(id)!!.toDto())
            }
            post("/import") {
                val uid = call.requireUser(dao)
                val imported = RecipeZip.parse(call.receiveBytes(MAX_IMPORT_BYTES))
                // Copia independiente: nuevo id, propietario = usuario actual, imágenes nuevas en su carpeta.
                val id = dao.insertRecipe(uid, imported.input)
                try {
                    imported.images.forEach { dao.addPhoto(id, images.save(id, it)) }
                } catch (e: Exception) {
                    try {
                        dao.deleteRecipe(id)
                    } finally {
                        images.deleteRecipeDir(id)
                    }
                    throw e
                }
                call.respond(HttpStatusCode.Created, dao.getRecipe(id)!!.toDto())
            }
            route("/{id}") {
                get {
                    call.respond(call.ownedRecipe(dao).toDto())
                }
                put {
                    val recipe = call.ownedRecipe(dao)
                    val input = RecipeValidator.validate(call.receiveJson<RecipeRequest>())
                    dao.updateRecipe(recipe.id, input)
                    call.respond(dao.getRecipe(recipe.id)!!.toDto())
                }
                delete {
                    val recipe = call.ownedRecipe(dao)
                    dao.deleteRecipe(recipe.id)
                    images.deleteRecipeDir(recipe.id)
                    call.respond(HttpStatusCode.NoContent)
                }
                get("/export") {
                    val recipe = call.ownedRecipe(dao)
                    val zip = RecipeZip.export(recipe) { images.read(it) }
                    call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"receta.zip\"")
                    call.respondBytes(zip, ContentType.Application.Zip)
                }
                post("/images") {
                    val recipe = call.ownedRecipe(dao)
                    if (recipe.photos.size >= MAX_PHOTOS_PER_RECIPE) {
                        throw ApiException(HttpStatusCode.BadRequest, "Máximo $MAX_PHOTOS_PER_RECIPE fotografías por receta")
                    }
                    val bytes = call.receiveBytes(MAX_IMAGE_BYTES)
                    val path = images.save(recipe.id, bytes)
                    val photo = try {
                        dao.addPhoto(recipe.id, path)
                    } catch (e: Exception) {
                        images.delete(path)
                        throw e
                    }
                    call.respond(HttpStatusCode.Created, PhotoDto(photo.id, photo.path))
                }
                delete("/images/{imageId}") {
                    val recipe = call.ownedRecipe(dao)
                    val imageId = call.parameters["imageId"]?.toLongOrNull()
                        ?: throw ApiException(HttpStatusCode.BadRequest, "Identificador de imagen inválido")
                    val photo = dao.getPhoto(recipe.id, imageId)
                        ?: throw ApiException(HttpStatusCode.NotFound, "Imagen no encontrada")
                    dao.deletePhoto(photo.id)
                    images.delete(photo.path)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private suspend inline fun <reified T : Any> ApplicationCall.receiveJson(): T =
    try {
        receive<T>()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw ApiException(HttpStatusCode.BadRequest, "El cuerpo de la petición no es un JSON válido")
    }

/** Lee el cuerpo por bloques y aborta en cuanto supera [maxBytes], sin cargarlo completo antes de comprobarlo. */
private suspend fun ApplicationCall.receiveBytes(maxBytes: Int): ByteArray {
    val tooLarge = ApiException(HttpStatusCode.PayloadTooLarge, "El archivo es demasiado grande")
    val declared = request.headers[HttpHeaders.ContentLength]?.toLongOrNull()
    if (declared != null && declared > maxBytes) throw tooLarge

    val channel = receiveChannel()
    val out = ByteArrayOutputStream(minOf(declared ?: 64 * 1024L, maxBytes.toLong()).toInt())
    val chunk = ByteArray(16 * 1024)
    var total = 0
    while (true) {
        val n = channel.readAvailable(chunk, 0, chunk.size)
        if (n < 0) break
        total += n
        if (total > maxBytes) throw tooLarge
        out.write(chunk, 0, n)
    }
    require(total > 0) { "El cuerpo de la petición está vacío" }
    return out.toByteArray()
}

private fun ApplicationCall.bearerToken(): String? =
    request.headers[HttpHeaders.Authorization]?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")?.trim()

private fun ApplicationCall.requireUser(dao: RecetarioDao): Long {
    val token = bearerToken() ?: throw ApiException(HttpStatusCode.Unauthorized, "Sesión requerida")
    return dao.userIdForToken(token) ?: throw ApiException(HttpStatusCode.Unauthorized, "Sesión inválida o expirada")
}

/** Devuelve la receta solo si pertenece al usuario autenticado (404 en caso contrario). */
private fun ApplicationCall.ownedRecipe(dao: RecetarioDao): Recipe {
    val uid = requireUser(dao)
    val id = parameters["id"]?.toLongOrNull() ?: throw ApiException(HttpStatusCode.BadRequest, "Identificador de receta inválido")
    val recipe = dao.getRecipe(id)
    if (recipe == null || recipe.userId != uid) throw ApiException(HttpStatusCode.NotFound, "Receta no encontrada")
    return recipe
}
