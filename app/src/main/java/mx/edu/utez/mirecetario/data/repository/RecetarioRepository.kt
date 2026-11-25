package mx.edu.utez.mirecetario.data.repository

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import mx.edu.utez.mirecetario.data.remote.ApiService
import mx.edu.utez.mirecetario.data.remote.AuthResponse
import mx.edu.utez.mirecetario.data.remote.ErrorDto
import mx.edu.utez.mirecetario.data.remote.LoginRequest
import mx.edu.utez.mirecetario.data.remote.PhotoDto
import mx.edu.utez.mirecetario.data.remote.RecipeDto
import mx.edu.utez.mirecetario.data.remote.RecipeRequest
import mx.edu.utez.mirecetario.data.remote.RegisterRequest
import mx.edu.utez.mirecetario.data.remote.ServerHost
import mx.edu.utez.mirecetario.data.remote.UserDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.io.OutputStream

private const val TAG = "RecetarioRepo"

class ApiError(message: String, val code: Int) : Exception(message)

/** Único punto por el que la UI habla con la API local (siempre vía Retrofit, fuera del hilo principal). */
class RecetarioRepository(val session: SessionStore) {

    /** Se activa cuando la API rechaza el token guardado (sesión caducada o eliminada). */
    val sesionExpirada = MutableStateFlow(false)

    /** [conSesion] = false para login/registro, donde un 401 significa credenciales incorrectas. */
    private suspend fun <T> call(conSesion: Boolean = true, block: suspend (ApiService) -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block(ServerHost.api()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 401 && conSesion && session.token != null) {
                session.clear()
                sesionExpirada.value = true
            }
            val message = try {
                Gson().fromJson(e.response()?.errorBody()?.string(), ErrorDto::class.java)?.error
            } catch (_: Exception) {
                null
            }
            Result.failure(ApiError(message ?: "Error ${e.code()}", e.code()))
        } catch (e: IOException) {
            Log.w(TAG, "Fallo de comunicación con la API local: ${e.javaClass.simpleName}")
            Result.failure(ApiError("No se pudo conectar con la API local. Intenta de nuevo", 0))
        } catch (e: IllegalStateException) {
            Log.w(TAG, "La API local no está disponible: ${e.message}")
            Result.failure(ApiError("La API local no pudo iniciarse. Cierra y vuelve a abrir la app", 0))
        } catch (e: Exception) {
            Log.w(TAG, "Error inesperado: ${e.javaClass.simpleName}")
            Result.failure(ApiError("Error inesperado (${e.javaClass.simpleName})", 0))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<UserDto> =
        call(conSesion = false) { it.register(RegisterRequest(name, email, password)) }.onSuccess { saveSession(it) }.map { it.user }

    suspend fun login(email: String, password: String): Result<UserDto> =
        call(conSesion = false) { it.login(LoginRequest(email, password)) }.onSuccess { saveSession(it) }.map { it.user }

    private fun saveSession(auth: AuthResponse) {
        session.token = auth.token
        session.enCursoId = -1L
    }

    suspend fun logout() {
        call { it.logout() }
        session.clear()
    }

    suspend fun me(): Result<UserDto> = call { it.me() }

    suspend fun uploadAvatar(bytes: ByteArray): Result<UserDto> =
        call { it.uploadAvatar(bytes.toRequestBody("image/*".toMediaType())) }

    suspend fun deleteAvatar(): Result<UserDto> =
        call { it.deleteAvatar() }

    suspend fun recipes(): Result<List<RecipeDto>> = call { it.recipes() }
    suspend fun recipe(id: Long): Result<RecipeDto> = call { it.recipe(id) }
    suspend fun createRecipe(req: RecipeRequest): Result<RecipeDto> = call { it.createRecipe(req) }
    suspend fun updateRecipe(id: Long, req: RecipeRequest): Result<RecipeDto> = call { it.updateRecipe(id, req) }
    suspend fun deleteRecipe(id: Long): Result<Unit> = call { it.deleteRecipe(id) }.onSuccess {
        if (session.enCursoId == id) session.enCursoId = -1L
    }

    suspend fun uploadImage(id: Long, bytes: ByteArray): Result<PhotoDto> =
        call { it.uploadImage(id, bytes.toRequestBody("image/*".toMediaType())) }

    suspend fun deleteImage(id: Long, imageId: Long): Result<Unit> = call { it.deleteImage(id, imageId) }

    /** Copia el ZIP directamente al destino, sin conservarlo completo en memoria. */
    suspend fun exportRecipe(id: Long, destino: OutputStream): Result<Unit> = call { api ->
        api.exportRecipe(id).use { it.byteStream().copyTo(destino) }
        Unit
    }

    suspend fun importRecipe(zip: ByteArray): Result<RecipeDto> =
        call { it.importRecipe(zip.toRequestBody("application/zip".toMediaType())) }
}
