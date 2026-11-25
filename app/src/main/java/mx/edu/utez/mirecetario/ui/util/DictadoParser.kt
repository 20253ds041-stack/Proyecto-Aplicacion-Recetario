package mx.edu.utez.mirecetario.ui.util

import java.text.Normalizer

/** Interpretación sencilla de frases dictadas en español (sin NLP): dificultad y tiempos. */
object DictadoParser {
    private val UNIDADES = mapOf(
        "cero" to 0, "un" to 1, "uno" to 1, "una" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5,
        "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10, "once" to 11, "doce" to 12,
        "trece" to 13, "catorce" to 14, "quince" to 15, "dieciseis" to 16, "diecisiete" to 17,
        "dieciocho" to 18, "diecinueve" to 19, "veinte" to 20, "veintiun" to 21, "veintiuno" to 21,
        "veintiuna" to 21, "veintidos" to 22, "veintitres" to 23, "veinticuatro" to 24, "veinticinco" to 25,
        "veintiseis" to 26, "veintisiete" to 27, "veintiocho" to 28, "veintinueve" to 29
    )
    private val DECENAS = mapOf(
        "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50, "sesenta" to 60,
        "setenta" to 70, "ochenta" to 80, "noventa" to 90, "cien" to 100
    )
    private val HORAS_Y_MEDIA = Regex("(\\d+)\\s*horas?\\s*y\\s*media")
    private val MEDIA_HORA = Regex("media\\s*hora")
    private val HORAS = Regex("(\\d+(?:[.,]\\d+)?)\\s*(?:horas?|hrs?|h)\\b")
    private val MINUTOS = Regex("(\\d+)\\s*(?:minutos?|mins?|m)\\b")
    private val HORAS_Y_NUMERO = Regex("(?:horas?|hrs?|h)\\s*(?:y\\s*)?(\\d+)\\s*$")
    private val SOLO_NUMERO = Regex("^\\D*(\\d+)\\D*$")

    /** 1 = fácil, 2 = media, 3 = difícil; null si no se reconoce. */
    fun dificultad(texto: String): Int? {
        val t = normalizar(texto)
        return when {
            Regex("dificil|complicad|compleja").containsMatchIn(t) -> 3
            Regex("facil|sencill|simple").containsMatchIn(t) -> 1
            Regex("media|medio|moderad|intermedi|normal|regular").containsMatchIn(t) -> 2
            else -> null
        }
    }

    /** Minutos totales ("30 minutos", "una hora y media", "cuarenta y cinco"); null si no se reconoce. */
    fun minutos(texto: String): Int? {
        val t = numerosADigitos(normalizar(texto))
        HORAS_Y_MEDIA.find(t)?.let { return it.groupValues[1].toInt() * 60 + 30 }
        var total = 0
        var encontrado = false
        HORAS.find(t)?.let {
            total += (it.groupValues[1].replace(',', '.').toDouble() * 60).toInt()
            encontrado = true
        }
        if (!encontrado && MEDIA_HORA.containsMatchIn(t)) {
            total += 30
            encontrado = true
        }
        val minutos = MINUTOS.find(t)
        if (minutos != null) {
            total += minutos.groupValues[1].toInt()
            encontrado = true
        } else if (encontrado) {
            // "1 hora 30": el número final sin unidad se interpreta como minutos
            HORAS_Y_NUMERO.find(t)?.let { total += it.groupValues[1].toInt() }
        }
        if (encontrado) return total
        return SOLO_NUMERO.find(t)?.groupValues?.get(1)?.toIntOrNull()
    }

    fun capitalizar(texto: String): String = texto.trim().replaceFirstChar { it.uppercase() }

    private fun normalizar(s: String): String =
        Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").trim()

    private fun numerosADigitos(t: String): String {
        val tokens = t.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val out = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val decena = DECENAS[tokens[i]]
            if (decena != null) {
                val unidad = if (i + 2 < tokens.size && tokens[i + 1] == "y") UNIDADES[tokens[i + 2]]?.takeIf { it in 1..9 } else null
                if (unidad != null) {
                    out += (decena + unidad).toString()
                    i += 3
                } else {
                    out += decena.toString()
                    i++
                }
                continue
            }
            out += UNIDADES[tokens[i]]?.toString() ?: tokens[i]
            i++
        }
        return out.joinToString(" ")
    }
}
