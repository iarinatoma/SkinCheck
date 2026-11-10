package com.example.skincheck.data

import android.content.ContentValues
import android.content.Context
import com.example.skincheck.R
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// The answer of register/login: either it worked (with the user) or an error message
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val messageRes: Int) : AuthResult()
}

// The only place in the app that talks to the "users" table
class UserRepository(context: Context) {

    private val db = SkinCheckDatabase(context.applicationContext)

    fun register(username: String, email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (exists("username", username)) return AuthResult.Error(R.string.error_username_taken)
        if (exists("email", cleanEmail)) return AuthResult.Error(R.string.error_email_taken)

        val salt = newSalt()
        val values = ContentValues().apply {
            put("username", username)
            put("email", cleanEmail)
            put("password_hash", hash(password, salt))
            put("salt", salt)
            put("created_at", System.currentTimeMillis())
        }
        val id = db.writableDatabase.insert("users", null, values)
        return if (id == -1L) AuthResult.Error(R.string.error_generic)
        else AuthResult.Success(User(id, username, cleanEmail, null))
    }

    fun login(email: String, password: String): AuthResult {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT id, username, email, password_hash, salt, skin_type FROM users WHERE email = ?",
            arrayOf(email.trim().lowercase())
        )
        cursor.use {
            if (!it.moveToFirst()) return AuthResult.Error(R.string.error_wrong_credentials)
            val storedHash = it.getString(3)
            val salt = it.getString(4)
            if (hash(password, salt) != storedHash) return AuthResult.Error(R.string.error_wrong_credentials)
            return AuthResult.Success(User(it.getLong(0), it.getString(1), it.getString(2), it.getString(5)))
        }
    }

    fun getUser(id: Long): User? {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT id, username, email, skin_type FROM users WHERE id = ?",
            arrayOf(id.toString())
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return User(it.getLong(0), it.getString(1), it.getString(2), it.getString(3))
        }
    }

    fun updateSkinType(userId: Long, skinType: String) {
        val values = ContentValues().apply { put("skin_type", skinType) }
        db.writableDatabase.update("users", values, "id = ?", arrayOf(userId.toString()))
    }

    private fun exists(column: String, value: String): Boolean {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT 1 FROM users WHERE $column = ? LIMIT 1", arrayOf(value)
        )
        cursor.use { return it.moveToFirst() }
    }

    // A random "salt" makes two equal passwords look different in the database
    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    // Turns a password into a fingerprint that cannot be turned back into the password
    private fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 10_000, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        return key.encoded.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
