package com.example.login.model

data class UserModel(
    val name: String,
    val email: String,
    val passwd: String,
    val secretAnswer: String = "" // Para la pregunta secreta de recuperación
)

// Clase para manejar las respuestas de validación
data class ValidationResponse(
    val isValid: Boolean,
    val message: String = ""
)

// Clase para manejar el estado del login
data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val user: UserModel? = null
)
