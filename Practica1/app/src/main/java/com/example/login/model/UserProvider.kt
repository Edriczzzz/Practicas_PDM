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
        println("DEBUG: UserProvider init - Ruta del archivo: ${userDataFile.absolutePath}")
        loadUsersFromStorage()
    }

    // Cargar usuarios desde storage interno o crear por defecto
    private fun loadUsersFromStorage() {
        try {
            println("DEBUG: Verificando si existe archivo: ${userDataFile.exists()}")
            if (userDataFile.exists()) {
                // Leer desde storage interno
                val jsonString = userDataFile.readText()
                println("DEBUG: Contenido del archivo JSON: $jsonString")
                if (jsonString.isNotBlank()) {
                    val userListType = object : TypeToken<List<UserModel>>() {}.type
                    val loadedUsers = gson.fromJson<List<UserModel>>(jsonString, userListType)
                    users = loadedUsers?.toMutableList() ?: mutableListOf()
                    println("DEBUG: Usuarios cargados desde archivo: ${users.size}")
                } else {
                    println("DEBUG: Archivo JSON vacío, creando usuarios por defecto")
                    createDefaultUsers()
                }
            } else {
                println("DEBUG: Archivo no existe, intentando cargar desde assets")
                // Intentar cargar desde assets primero
                if (!loadUsersFromAssets()) {
                    println("DEBUG: No se pudo cargar desde assets, creando usuarios por defecto")
                    createDefaultUsers()
                }
                // Guardar al storage interno
                saveUsersToStorage()
            }
        } catch (e: Exception) {
            println("ERROR: Exception en loadUsersFromStorage: ${e.message}")
            e.printStackTrace()
            createDefaultUsers()
            saveUsersToStorage()
        }
    }

    // Cargar usuarios desde assets/users.json
    private fun loadUsersFromAssets(): Boolean {
        return try {
            val inputStream = context.assets.open("users.json")
            val reader = InputStreamReader(inputStream)
            val userListType = object : TypeToken<List<UserModel>>() {}.type
            val loadedUsers = gson.fromJson<List<UserModel>>(reader, userListType)
            users = loadedUsers?.toMutableList() ?: mutableListOf()
            reader.close()
            inputStream.close()
            println("DEBUG: Usuarios cargados desde assets: ${users.size}")
            true
        } catch (e: Exception) {
            println("ERROR: No se pudo cargar desde assets: ${e.message}")
            false
        }
    }

    // Guardar usuarios en storage interno con debug mejorado
    private fun saveUsersToStorage() {
        try {
            println("DEBUG: Intentando guardar usuarios en: ${userDataFile.absolutePath}")
            println("DEBUG: Número de usuarios a guardar: ${users.size}")

            // Verificar contenido antes de guardar
            users.forEach { user ->
                println("DEBUG: Usuario a guardar - Email: ${user.email}, Pass: ${user.passwd}")
            }

            val jsonString = gson.toJson(users)
            println("DEBUG: JSON a guardar: $jsonString")

            FileWriter(userDataFile).use { writer ->
                writer.write(jsonString)
                writer.flush()
            }

            println("DEBUG: Archivo guardado exitosamente")

            // Verificar que se guardó correctamente
            if (userDataFile.exists()) {
                val savedContent = userDataFile.readText()
                println("DEBUG: Contenido verificado después de guardar: $savedContent")
            }

        } catch (e: Exception) {
            println("ERROR: Exception en saveUsersToStorage: ${e.message}")
            e.printStackTrace()
        }
    }

    // Crear usuarios por defecto
    private fun createDefaultUsers() {
        users = mutableListOf(
            UserModel(
                name = "Juan Pérez",
                email = "juan@email.com",
                passwd = "Password123#",
                secretQuestion = "¿Cuál es el nombre de tu primera mascota?",
                secretAnswer = "firulais"
            ),
            UserModel(
                name = "María García",
                email = "maria@email.com",
                passwd = "Secure456$",
                secretQuestion = "¿En qué ciudad naciste?",
                secretAnswer = "guadalajara"
            ),
            UserModel(
                name = "Carlos López",
                email = "carlos@email.com",
                passwd = "MyPass789_",
                secretQuestion = "¿Cuál es tu color favorito?",
                secretAnswer = "azul"
            ),
            UserModel(
                name = "Ana Rodríguez",
                email = "ana@email.com",
                passwd = "Strong012?",
                secretQuestion = "¿Cómo se llama tu mejor amigo de la infancia?",
                secretAnswer = "luis"
            ),
            UserModel(
                name = "Admin User",
                email = "admin@email.com",
                passwd = "Admin789_",
                secretQuestion = "¿Cuál es tu palabra secreta?",
                secretAnswer = "admin"
            )
        )
        println("DEBUG: Usuarios por defecto creados: ${users.size}")
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
        println("DEBUG: Login attempt - Email: $email, Found user: ${user != null}")

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

    // Cambiar contraseña (desde menú del usuario logueado)
    fun changePassword(email: String, currentPassword: String, newPassword: String): ValidationResponse {
        println("DEBUG: changePassword llamado - Email: $email")

        if (!isValidPassword(newPassword)) {
            return ValidationResponse(false, "La nueva contraseña no cumple con los requisitos")
        }

        val userIndex = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (userIndex != -1) {
            val user = users[userIndex]
            println("DEBUG: Usuario encontrado - Contraseña actual: ${user.passwd}")

            if (user.passwd == currentPassword) {
                println("DEBUG: Contraseña actual correcta, cambiando a: $newPassword")
                users[userIndex] = user.copy(passwd = newPassword)

                // Verificar el cambio en memoria
                println("DEBUG: Contraseña cambiada en memoria: ${users[userIndex].passwd}")

                // Guardar cambios
                saveUsersToStorage()
                return ValidationResponse(true, "Contraseña cambiada exitosamente")
            } else {
                println("DEBUG: Contraseña actual incorrecta")
                return ValidationResponse(false, "Contraseña actual incorrecta")
            }
        }
        println("DEBUG: Usuario no encontrado")
        return ValidationResponse(false, "Usuario no encontrado")
    }

    // Restablecer contraseña después de recovery
    fun resetPassword(email: String, newPassword: String): ValidationResponse {
        println("DEBUG: resetPassword llamado - Email: $email, Nueva contraseña: $newPassword")

        if (!isValidPassword(newPassword)) {
            return ValidationResponse(false, "La contraseña no cumple con los requisitos")
        }

        val userIndex = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (userIndex != -1) {
            val user = users[userIndex]
            println("DEBUG: Usuario encontrado para reset - Contraseña anterior: ${user.passwd}")

            users[userIndex] = user.copy(passwd = newPassword)
            println("DEBUG: Contraseña actualizada en memoria: ${users[userIndex].passwd}")

            // IMPORTANTE: Guardar cambios inmediatamente
            saveUsersToStorage()
            return ValidationResponse(true, "Contraseña restablecida exitosamente")
        }
        println("DEBUG: Usuario no encontrado para reset")
        return ValidationResponse(false, "Usuario no encontrado")
    }

    // Validar formato de email
    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }

    // Validar contraseña
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { it in "_.$#?" }

        return hasUpperCase && hasDigit && hasSymbol
    }
}