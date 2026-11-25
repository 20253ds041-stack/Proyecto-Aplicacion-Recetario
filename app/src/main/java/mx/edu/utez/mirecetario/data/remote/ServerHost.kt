package mx.edu.utez.mirecetario.data.remote

import android.content.Context
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import mx.edu.utez.mirecetario.data.local.ImageStore
import mx.edu.utez.mirecetario.data.local.RecetarioDao
import mx.edu.utez.mirecetario.data.local.RecetarioDb
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetAddress
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.Proxy
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Arranca el servidor Ktor en 127.0.0.1 (puerto libre elegido en tiempo de ejecución) y expone el
 * cliente Retrofit solo cuando el servidor responde en /health: quien llame a [api] espera a que esté listo.
 * Si el arranque falla, la siguiente llamada a [api] lo reintenta. El servidor vive lo que el proceso de la app.
 */
object ServerHost {
    @Volatile private var ready = CompletableDeferred<ApiService>()
    private var started = false
    private var appContext: Context? = null
    private var tokenProvider: (() -> String?)? = null
    private var server: EmbeddedServer<*, *>? = null

    fun start(context: Context, tokenProvider: () -> String?) {
        val deferred: CompletableDeferred<ApiService>
        synchronized(this) {
            if (started) return
            started = true
            appContext = context.applicationContext
            this.tokenProvider = tokenProvider
            deferred = ready
        }
        val app = context.applicationContext
        thread(name = "recetario-api-start", isDaemon = true) {
            try {
                val dao = RecetarioDao(RecetarioDb(app))
                val images = ImageStore(app.filesDir)
                runCatching { images.deleteOrphans(dao.allPhotoPaths() + dao.allAvatarPaths()) }
                val port = launchServer(dao, images)
                deferred.complete(buildClient(port, tokenProvider))
            } catch (t: Throwable) {
                stop()
                synchronized(this) { started = false }
                deferred.completeExceptionally(t)
            }
        }
    }

    /** Espera a que la API esté lista; si el arranque anterior falló, lo reintenta una vez. */
    suspend fun api(): ApiService {
        try {
            return ready.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // arranque fallido: se reintenta abajo
        }
        val ctx: Context?
        val provider: (() -> String?)?
        synchronized(this) {
            if (!started && ready.isCompleted) ready = CompletableDeferred()
            ctx = appContext
            provider = tokenProvider
        }
        if (ctx != null && provider != null) start(ctx, provider)
        return ready.await()
    }

    fun stop() {
        val s = synchronized(this) { server.also { server = null } }
        runCatching { s?.stop(200, 500) }
    }

    /** Prueba hasta 3 puertos: si otro proceso ocupa el elegido, /health no responderá como nuestra API. */
    private fun launchServer(dao: RecetarioDao, images: ImageStore): Int {
        var lastError: Throwable? = null
        repeat(3) {
            val port = ServerSocket(0, 0, InetAddress.getByName("127.0.0.1")).use { it.localPort }
            var candidate: EmbeddedServer<*, *>? = null
            try {
                candidate = embeddedServer(CIO, port = port, host = "127.0.0.1") {
                    recetarioModule(dao, images)
                }.start(wait = false)
                if (awaitHealth(port)) {
                    synchronized(this) { server = candidate }
                    return port
                }
            } catch (e: Exception) {
                lastError = e
            }
            runCatching { candidate?.stop(0, 200) }
        }
        throw IllegalStateException("La API local no pudo iniciarse", lastError)
    }

    private fun awaitHealth(port: Int): Boolean {
        repeat(40) {
            try {
                val conn = URL("http://127.0.0.1:$port/health").openConnection(Proxy.NO_PROXY) as HttpURLConnection
                try {
                    conn.connectTimeout = 300
                    conn.readTimeout = 500
                    if (conn.responseCode == 200 && conn.inputStream.bufferedReader().readText().contains("\"recetario\"")) {
                        return true
                    }
                } finally {
                    conn.disconnect()
                }
            } catch (e: IOException) {
                // todavía no acepta conexiones
            }
            Thread.sleep(100)
        }
        return false
    }

    private fun buildClient(port: Int, tokenProvider: () -> String?): ApiService {
        val http = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = tokenProvider()
                val request = if (token != null) {
                    chain.request().newBuilder().header("Authorization", "Bearer $token").build()
                } else chain.request()
                chain.proceed(request)
            }
            .build()
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:$port/")
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
