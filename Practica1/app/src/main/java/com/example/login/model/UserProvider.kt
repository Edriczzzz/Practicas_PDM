package com.example.login.provider

import android.content.Context
import com.example.login.model.UserModel
import com.example.login.model.ValidationResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class UserProvider(private val context: Context) {

    private var users: MutableList<UserModel> = mutableListOf()

    init {
        loadUsersFromJson()
    }

    // Cargar usuarios desde assets/users.json
    private fun loadUsersFromJson() {
        try {
            val jsonString = context.assets.open("users.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            users.clear()
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                val user = UserModel(
                    name = userJson.getString("name"),
                    email = userJson.getString("email"),
                    passwd = userJson.getString("passwd"),
                    secretAnswer = userJson.optString("secretAnswer", "")
                )
                users.add(user)
            }
        } catch (e: IOException) {
            // Si no existe el archivo, crear usuarios por defecto
            createDefaultUsers()
        }
    }

    // Crear usuarios por defecto si no existe el JSON
    private fun createDefaultUsers() {
        users = mutableListOf(
            UserModel(
                name = "Juan Pérez",
                email = "juan@email.com",
                passwd = "Password123#",
                secretAnswer = "firulais"
            ),
            UserModel(
                name = "María García",
                email = "maria@email.com",
                passwd = "Secure456$",
                secretAnswer = "michi"
            ),
            UserModel(
                name = "Admin User",
                email = "admin@email.com",
                passwd = "Admin789_",
                secretAnswer = "admin"
            )
        )
    }

    // Validar credenciales de login
    fun validateLogin(email: String, password: String): ValidationResponse {
        if (!isValidEmail(email)) {
            return ValidationResponse(false, "Formato de email inválido")
        }

        if (!isValidPassword(password)) {
            return ValidationResponse(false, "La contraseña no cumple con los requisitos")
        }

        val user = users.find { it.email.equals(email, ignoreCase = true) && it.passwd == password }
        return if (user != null) {
            ValidationResponse(true, "Login exitoso")
        } else {
            ValidationResponse(false, "Email o contraseña incorrectos")
        }
    }

    // Obtener usuario por email
    fun getUserByEmail(email: String): UserModel? {
        return users.find { it.email.equals(email, ignoreCase = true) }
    }

    // Validar recuperación de contraseña
    fun validateRecovery(email: String, secretAnswer: String): ValidationResponse {
        if (!isValidEmail(email)) {
            return ValidationResponse(false, "Formato de email inválido")
        }

        val user = users.find { it.email.equals(email, ignoreCase = true) }
        return if (user != null && user.secretAnswer.equals(secretAnswer, ignoreCase = true)) {
            ValidationResponse(true, "Verificación exitosa")
        } else {
            ValidationResponse(false, "Email o respuesta secreta incorrectos")
        }
    }

    // Cambiar contraseña
    fun changePassword(email: String, currentPassword: String, newPassword: String): ValidationResponse {
        if (!isValidPassword(newPassword)) {
            return ValidationResponse(false, "La nueva contraseña no cumple con los requisitos")
        }

        val userIndex = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (userIndex != -1) {
            val user = users[userIndex]
            if (user.passwd == currentPassword) {
                users[userIndex] = user.copy(passwd = newPassword)
                return ValidationResponse(true, "Contraseña cambiada exitosamente")
            } else {
                return ValidationResponse(false, "Contraseña actual incorrecta")
            }
        }
        return ValidationResponse(false, "Usuario no encontrado")
    }

    // Cambiar contraseña después de recovery
    fun resetPassword(email: String, newPassword: String): ValidationResponse {
        if (!isValidPassword(newPassword)) {
            return ValidationResponse(false, "La contraseña no cumple con los requisitos")
        }

        val userIndex = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (userIndex != -1) {
            val user = users[userIndex]
            users[userIndex] = user.copy(passwd = newPassword)
            return ValidationResponse(true, "Contraseña restablecida exitosamente")
        }
        return ValidationResponse(false, "Usuario no encontrado")
    }

    // Validar formato de email con regex
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }

    // Validar contraseña con regex (mín 8, mayúscula, dígito, símbolo)
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { it in "_.$#?" }

        return hasUpperCase && hasDigit && hasSymbol
    }
}