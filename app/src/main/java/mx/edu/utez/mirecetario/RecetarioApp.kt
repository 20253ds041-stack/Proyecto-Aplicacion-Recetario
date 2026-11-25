package mx.edu.utez.mirecetario

import android.app.Application
import mx.edu.utez.mirecetario.data.remote.ServerHost
import mx.edu.utez.mirecetario.data.repository.RecetarioRepository
import mx.edu.utez.mirecetario.data.repository.SessionStore

class RecetarioApp : Application() {
    lateinit var repository: RecetarioRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val session = SessionStore(this)
        repository = RecetarioRepository(session)
        // Arranca la API REST local en segundo plano; el repositorio espera a que esté lista.
        ServerHost.start(this) { session.token }
    }
}
