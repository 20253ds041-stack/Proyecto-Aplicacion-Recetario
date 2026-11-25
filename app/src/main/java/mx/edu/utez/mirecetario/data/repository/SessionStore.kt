package mx.edu.utez.mirecetario.data.repository

import android.content.Context

/** Sesión local: token de la API y receta "en curso" del usuario actual. */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit().apply { if (value == null) remove("token") else putString("token", value) }.apply()

    var enCursoId: Long
        get() = prefs.getLong("en_curso_id", -1L)
        set(value) = prefs.edit().putLong("en_curso_id", value).apply()

    var primaryColorInt: Int
        get() = prefs.getInt("primary_color", 0xFF5476AA.toInt())
        set(value) = prefs.edit().putInt("primary_color", value).apply()

    fun clear() = prefs.edit().clear().apply()
}
