package mx.edu.utez.mirecetario.data.local

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Hash de contraseñas con PBKDF2WithHmacSHA256 + salt aleatorio. */
object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256

    fun newSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hash(password: String, saltBase64: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(saltBase64), ITERATIONS, KEY_BITS)
        try {
            val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return Base64.getEncoder().encodeToString(key)
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(password: String, saltBase64: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(
            hash(password, saltBase64).toByteArray(),
            expectedHash.toByteArray()
        )
}
