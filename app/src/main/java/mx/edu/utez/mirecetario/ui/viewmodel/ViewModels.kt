package mx.edu.utez.mirecetario.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import mx.edu.utez.mirecetario.RecetarioApp
import mx.edu.utez.mirecetario.data.local.RecipeZip
import mx.edu.utez.mirecetario.data.remote.RecipeDto
import mx.edu.utez.mirecetario.data.remote.UserDto
import mx.edu.utez.mirecetario.data.repository.ApiError
import mx.edu.utez.mirecetario.data.repository.RecetarioRepository
import mx.edu.utez.mirecetario.ui.util.RecetaFormState

/** Como Result.fold, pero con callbacks Unit (evita problemas de inferencia con lambdas de varias sentencias). */
private inline fun <T> Result<T>.resolver(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit) {
    val error = exceptionOrNull()
    if (error == null) onSuccess(getOrThrow()) else onFailure(error)
}

private const val MAX_FOTO_BYTES = 25 * 1024 * 1024
private const val MAX_FOTOS = 12

/** Base: acceso al repositorio, avisos (Toast) y lectura de archivos elegidos por el usuario. */
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {
    protected val repo: RecetarioRepository = (app as RecetarioApp).repository

    protected fun aviso(mensaje: String?) {
        if (!mensaje.isNullOrBlank()) Toast.makeText(getApplication<Application>(), mensaje, Toast.LENGTH_LONG).show()
    }

    /** Lee el archivo por bloques; devuelve null si no se puede leer o supera [maximo] bytes. */
    protected suspend fun leerBytes(uri: Uri, maximo: Int = MAX_FOTO_BYTES): ByteArray? = withContext(Dispatchers.IO) {
        try {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(16 * 1024)
                var total = 0
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    total += n
                    if (total > maximo) return@withContext null
                    out.write(buffer, 0, n)
                }
                out.toByteArray()
            }
        } catch (e: Exception) {
            null
        }
    }
}

sealed interface SesionEstado {
    data object Cargando : SesionEstado
    data class Activa(val user: UserDto) : SesionEstado
    data object Cerrada : SesionEstado
}

/** Sesión local del usuario (alcance: Activity). */
class SessionViewModel(app: Application) : BaseViewModel(app) {
    var estado by mutableStateOf<SesionEstado>(SesionEstado.Cargando)
        private set
    var trabajando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        // Si la API rechaza el token (caducó), se vuelve al estado "sesión cerrada".
        viewModelScope.launch {
            repo.sesionExpirada.collect { expirada ->
                if (expirada) {
                    estado = SesionEstado.Cerrada
                    repo.sesionExpirada.value = false
                    aviso("Tu sesión expiró. Inicia sesión de nuevo")
                }
            }
        }
        viewModelScope.launch {
            if (repo.session.token == null) {
                estado = SesionEstado.Cerrada
            } else {
                repo.me().resolver(
                    onSuccess = { estado = SesionEstado.Activa(it) },
                    onFailure = {
                        if ((it as? ApiError)?.code == 401) repo.session.clear()
                        estado = SesionEstado.Cerrada
                    }
                )
            }
        }
    }

    fun limpiarError() { error = null }

    fun iniciarSesion(email: String, password: String, onOk: () -> Unit) {
        if (email.isBlank() || password.isEmpty()) { error = "Ingresa tu correo y contraseña"; return }
        autenticar(onOk) { repo.login(email.trim(), password) }
    }

    fun registrar(nombre: String, email: String, password: String, onOk: () -> Unit) {
        when {
            nombre.isBlank() -> { error = "Ingresa tu nombre"; return }
            !Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email.trim()) -> { error = "Ingresa un correo válido"; return }
            password.length < 6 -> { error = "La contraseña debe tener al menos 6 caracteres"; return }
        }
        autenticar(onOk) { repo.register(nombre.trim(), email.trim(), password) }
    }

    private fun autenticar(onOk: () -> Unit, accion: suspend () -> Result<UserDto>) {
        if (trabajando) return
        viewModelScope.launch {
            trabajando = true
            error = null
            val resultado = accion()
            resultado.onSuccess { estado = SesionEstado.Activa(it); onOk() }
                .onFailure { error = it.message }
            trabajando = false
        }
    }

    fun cerrarSesion(onOk: () -> Unit) {
        viewModelScope.launch {
            repo.logout()
            estado = SesionEstado.Cerrada
            onOk()
        }
    }

    fun subirAvatar(uri: Uri) {
        if (trabajando) return
        viewModelScope.launch {
            trabajando = true
            val bytes = leerBytes(uri)
            if (bytes == null) {
                aviso("No se pudo leer la imagen seleccionada")
                trabajando = false
                return@launch
            }
            repo.uploadAvatar(bytes).resolver(
                onSuccess = {
                    estado = SesionEstado.Activa(it)
                    aviso("Foto de perfil actualizada")
                },
                onFailure = { aviso("No se pudo actualizar la foto: ${it.message}") }
            )
            trabajando = false
        }
    }

    fun eliminarAvatar() {
        if (trabajando) return
        viewModelScope.launch {
            trabajando = true
            repo.deleteAvatar().resolver(
                onSuccess = {
                    estado = SesionEstado.Activa(it)
                    aviso("Foto de perfil eliminada")
                },
                onFailure = { aviso("No se pudo eliminar la foto: ${it.message}") }
            )
            trabajando = false
        }
    }
}

/** Lista de recetas del usuario e importación de ZIP. */
class RecetasGuardadasViewModel(app: Application) : BaseViewModel(app) {
    var recetas by mutableStateOf<List<RecipeDto>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set

    fun cargar() {
        viewModelScope.launch {
            cargando = true
            repo.recipes().resolver(onSuccess = { recetas = it }, onFailure = { aviso(it.message) })
            cargando = false
        }
    }

    fun importar(uri: Uri) {
        viewModelScope.launch {
            val bytes = leerBytes(uri, RecipeZip.MAX_ZIP_BYTES)
            if (bytes == null) { aviso("No se pudo leer el archivo (máximo ${RecipeZip.MAX_ZIP_BYTES / (1024 * 1024)} MB)"); return@launch }
            repo.importRecipe(bytes).resolver(
                onSuccess = { aviso("Receta \"${it.title}\" importada"); cargar() },
                onFailure = { aviso("No se pudo importar: ${it.message}") }
            )
        }
    }
}

/** Crear receta (formulario + fotos pendientes de guardar). */
class CrearRecetaViewModel(app: Application) : BaseViewModel(app) {
    val form = RecetaFormState()
    val fotos = mutableStateListOf<Uri>()
    var guardando by mutableStateOf(false)
        private set

    fun agregarFotos(uris: List<Uri>) {
        uris.forEach { if (it !in fotos && fotos.size < MAX_FOTOS) fotos.add(it) }
    }

    fun quitarFoto(uri: Uri) { fotos.remove(uri) }

    fun guardar(onOk: () -> Unit) {
        form.validar()?.let { aviso(it); return }
        if (guardando) return
        viewModelScope.launch {
            guardando = true
            val creada = repo.createRecipe(form.toRequest())
            val receta = creada.getOrNull()
            if (receta == null) {
                aviso(creada.exceptionOrNull()?.message)
                guardando = false
                return@launch
            }
            var fallos = 0
            for (uri in fotos.toList()) {
                val bytes = leerBytes(uri)
                if (bytes == null || repo.uploadImage(receta.id, bytes).isFailure) fallos++
            }
            aviso(if (fallos == 0) "Receta guardada" else "Receta guardada, pero $fallos foto(s) no se pudieron guardar")
            guardando = false
            onOk()
        }
    }
}

/** Información, edición, borrado, fotos y exportación de una receta. */
class RecetaDetalleViewModel(app: Application, private val id: Long) : BaseViewModel(app) {
    val form = RecetaFormState()
    var receta by mutableStateOf<RecipeDto?>(null)
        private set
    var cargando by mutableStateOf(true)
        private set
    var editando by mutableStateOf(false)
        private set
    var noEncontrada by mutableStateOf(false)
        private set

    fun cargar() {
        if (editando) return
        viewModelScope.launch {
            repo.recipe(id).resolver(
                onSuccess = { receta = it; form.cargar(it); noEncontrada = false },
                onFailure = { if ((it as? ApiError)?.code == 404) noEncontrada = true else aviso(it.message) }
            )
            cargando = false
        }
    }

    fun alternarEdicion() {
        if (!editando) { editando = true; return }
        form.validar()?.let { aviso(it); return }
        viewModelScope.launch {
            repo.updateRecipe(id, form.toRequest()).resolver(
                onSuccess = { receta = it; form.cargar(it); editando = false; aviso("Cambios guardados") },
                onFailure = { aviso(it.message) }
            )
        }
    }

    fun cancelarEdicion() {
        receta?.let { form.cargar(it) }
        editando = false
    }

    fun eliminar(onOk: () -> Unit) {
        viewModelScope.launch {
            repo.deleteRecipe(id).resolver(onSuccess = { aviso("Receta eliminada"); onOk() }, onFailure = { aviso(it.message) })
        }
    }

    private suspend fun refrescarReceta() {
        repo.recipe(id).onSuccess { receta = it }
    }

    fun agregarFotos(uris: List<Uri>) {
        viewModelScope.launch {
            var fallos = 0
            for (uri in uris) {
                val bytes = leerBytes(uri)
                if (bytes == null || repo.uploadImage(id, bytes).isFailure) fallos++
            }
            if (fallos > 0) aviso("$fallos foto(s) no se pudieron guardar")
            refrescarReceta()
        }
    }

    fun eliminarFoto(photoId: Long) {
        viewModelScope.launch {
            repo.deleteImage(id, photoId).resolver(onSuccess = { refrescarReceta() }, onFailure = { aviso(it.message) })
        }
    }

    fun exportar(destino: Uri) {
        viewModelScope.launch {
            val salida = withContext(Dispatchers.IO) {
                try {
                    getApplication<Application>().contentResolver.openOutputStream(destino)
                } catch (e: Exception) {
                    null
                }
            }
            if (salida == null) { aviso("No se pudo abrir el archivo de destino"); return@launch }
            val resultado = withContext(Dispatchers.IO) { salida.use { repo.exportRecipe(id, it) } }
            resultado.resolver(
                onSuccess = { aviso("Receta exportada") },
                onFailure = { aviso("No se pudo exportar: ${it.message}") }
            )
        }
    }
}

/** Receta en curso: id de la navegación o, si no hay, la última iniciada (persistida en la sesión). */
class RecetaEnCursoViewModel(app: Application, private val idNavegacion: Long) : BaseViewModel(app) {
    var receta by mutableStateOf<RecipeDto?>(null)
        private set
    var cargando by mutableStateOf(true)
        private set

    fun cargar() {
        if (idNavegacion > 0) {
            repo.session.enCursoId = idNavegacion
        }
        val targetId = repo.session.enCursoId
        if (targetId <= 0) {
            receta = null
            cargando = false
            return
        }
        viewModelScope.launch {
            cargando = true
            repo.recipe(targetId).resolver(
                onSuccess = { receta = it },
                onFailure = { receta = null; if ((it as? ApiError)?.code == 404) repo.session.enCursoId = -1L }
            )
            cargando = false
        }
    }

    fun terminar() {
        repo.session.enCursoId = -1L
        receta = null
    }
}

class RecetaIdFactory(private val app: Application, private val id: Long) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(RecetaDetalleViewModel::class.java) -> RecetaDetalleViewModel(app, id) as T
        modelClass.isAssignableFrom(RecetaEnCursoViewModel::class.java) -> RecetaEnCursoViewModel(app, id) as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
