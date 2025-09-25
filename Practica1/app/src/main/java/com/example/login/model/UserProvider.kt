package com.example.login.provider

import android.content.Context
import com.example.login.model.UserModel
import com.example.login.model.ValidationResponse
import com.example.login.model.RecoveryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

class UserProvider(private val context: Context) {

    private var users: MutableList<UserModel> = mutableListOf()
    private val gson = Gson()
    private val userDataFile = File(context.filesDir, "users.json")

    init {
        loadUsersFromStorage()
    }

    // Cargar usuarios desde storage interno o crear por defecto
    private fun loadUsersFromStorage() {
                val jsonString = userDataFile.readText()

                    val userListType = object : TypeToken<List<UserModel>>() {}.type
                    val loadedUsers = gson.fromJson<List<UserModel>>(jsonString, userListType)
                    users = loadedUsers?.toMutableList() ?: mutableListOf()

    }


    // Guardar usuarios
    private fun saveUsersToStorage() {
        try {
            val jsonString = gson.toJson(users)
            FileWriter(userDataFile).use { writer ->
                writer.write(jsonString)
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Validar credenciales de login
    fun validateLogin(email: String, password: String): ValidationResponse {
        if (email.isBlank()) {
            return ValidationResponse(false, "El email no puede estar vacío")
        }

        if (password.isBlank()) {
            return ValidationResponse(false, "La contraseña no puede estar vacía")
        }

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

    // Obtener pregunta secreta por email
    fun getSecretQuestion(email: String): RecoveryResponse {
        if (!isValidEmail(email)) {
            return RecoveryResponse(false, "Formato de email inválido")
        }

        val user = users.find { it.email.equals(email, ignoreCase = true) }
        return if (user != null) {
            RecoveryResponse(true, "Usuario encontrado", user.secretQuestion)
        } else {
            RecoveryResponse(false, "Email no encontrado")
        }
    }

    // Validar recuperación de contraseña
    fun validateRecovery(email: String, secretAnswer: String): ValidationResponse {
        if (!isValidEmail(email)) {
            return ValidationResponse(false, "Formato de email inválido")
        }

        val user = users.find { it.email.equals(email, ignoreCase = true) }
        return if (user != null && user.secretAnswer.equals(secretAnswer.trim(), ignoreCase = true)) {
            ValidationResponse(true, "Verificación exitosa")
        } else {
            ValidationResponse(false, "Respuesta secreta incorrecta")
        }
    }

    // Cambiar contraseña (
    fun changePassword(email: String, currentPassword: String, newPassword: String): ValidationResponse {
        if (!isValidPassword(newPassword)) {
            return ValidationResponse(false, "La nueva contraseña no cumple con los requisitos")
        }

        val userIndex = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (userIndex != -1) {
            val user = users[userIndex]
            if (user.passwd == currentPassword) {
                // Crear nuevo objeto UserModel con contraseña actualizada
                users[userIndex] = user.copy(passwd = newPassword)
                saveUsersToStorage()
                return ValidationResponse(true, "Contraseña cambiada exitosamente")
            } else {
                return ValidationResponse(false, "Contraseña actual incorrecta")
            }
        }
        return ValidationResponse(false, "Usuario no encontrado")
    }

    // Restablecer contraseña después de recovery
    fun resetPassword(email: String, newPassword: String): ValidationResponse {
        if (!isValidPassword(newPassword)) {
            return ValidationResponse(false, "La contraseña no cumple con los requisitos")
        }

        val userIndex = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (userIndex != -1) {
            val user = users[userIndex]
            users[userIndex] = user.copy(passwd = newPassword)
            saveUsersToStorage()
            return ValidationResponse(true, "Contraseña restablecida exitosamente")
        }
        return ValidationResponse(false, "Usuario no encontrado")
    }

    // Validar formato de email con regex
    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }

    // Validar contraseña con regex
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { it in "_.$#?" }

        return hasUpperCase && hasDigit && hasSymbol
    }

    // Método para debug listar todos los usuarios //

    fun getAllUsers(): List<UserModel> {
        return users.toList()
    }
}