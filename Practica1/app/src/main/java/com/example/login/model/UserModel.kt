package com.example.login.model

// Clase para el modelo de usuario con pregunta secreta personalizada
data class UserModel(
    val name: String,
    val email: String,
    var passwd: String,
    val secretQuestion: String,
    val secretAnswer: String
)

// Clase para manejar las respuestas de validación
data class ValidationResponse(
    val isValid: Boolean,
    val message: String = ""
)

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val user: UserModel? = null
)

data class RecoveryResponse(
    val isValid: Boolean,
    val message: String = "",
    val secretQuestion: String? = null
)