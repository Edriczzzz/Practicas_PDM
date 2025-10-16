package edu.ipn.upiita.pdm.navjpc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class FormRegViewModel : ViewModel() {
    // Estados de los campos
    var nombre by mutableStateOf("")
    var boleta by mutableStateOf("")
    var correo by mutableStateOf("")
    var carrera by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    // Mensajes de error
    var nombreError by mutableStateOf<String?>(null)
    var boletaError by mutableStateOf<String?>(null)
    var correoError by mutableStateOf<String?>(null)
    var carreraError by mutableStateOf<String?>(null)
    var passwordError by mutableStateOf<String?>(null)
    var confirmPasswordError by mutableStateOf<String?>(null)

    var registroExitoso by mutableStateOf(false)

    // Lista de carreras disponibles
    val carreras = listOf(
        "Ingeniería en Energía",
        "Ingeniería Biónica",
        "Ingeniería Mecatrónica",
        "Ingeniería Telemática"
    )

    fun validarCampos(): Boolean {
        var isValid = true

        // Validar nombre (solo letras y espacios)
        nombreError = when {
            nombre.isBlank() -> {
                isValid = false
                "El nombre es obligatorio"
            }
            !isNombreValido(nombre) -> {
                isValid = false
                "El nombre solo debe contener letras"
            }
            else -> null
        }

        // Validar boleta con regex específico
        boletaError = when {
            boleta.isBlank() -> {
                isValid = false
                "La boleta es obligatoria"
            }
            !isBoletaValida(boleta) -> {
                isValid = false
                "Formato de boleta inválido (debe ser XXXX64XXXX)"
            }
            else -> null
        }

        // Validar correo
        correoError = if (!isEmailValido(correo)) {
            isValid = false
            "Correo no válido"
        } else null

        // Validar carrera
        carreraError = if (carrera.isBlank()) {
            isValid = false
            "Debe seleccionar una carrera"
        } else null

        // Validar contraseña
        passwordError = when {
            password.length < 8 -> {
                isValid = false
                "La contraseña debe tener al menos 8 caracteres"
            }
            !isPasswordValida(password) -> {
                isValid = false
                "Debe contener mayúscula, minúscula y número"
            }
            else -> null
        }

        // Validar confirmación de contraseña
        confirmPasswordError = if (confirmPassword != password) {
            isValid = false
            "Las contraseñas no coinciden"
        } else null

        registroExitoso = isValid
        return isValid
    }

    private fun isNombreValido(nombre: String): Boolean {
        // Solo letras y espacios
        val pattern = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+\$")
        return pattern.matcher(nombre).matches()
    }

    private fun isBoletaValida(boleta: String): Boolean {
        // Formato: XXXX64XXXX (10 dígitos, con "64" en las posiciones 4-5)
        val pattern = Pattern.compile("^\\d{4}64\\d{4}\$")
        return pattern.matcher(boleta).matches()
    }

    private fun isEmailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValida(password: String): Boolean {
        // Al menos una mayúscula, una minúscula y un número
        val pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}\$")
        return pattern.matcher(password).matches()
    }

    fun limpiarFormulario() {
        nombre = ""
        boleta = ""
        correo = ""
        carrera = ""
        password = ""
        confirmPassword = ""
        nombreError = null
        boletaError = null
        correoError = null
        carreraError = null
        passwordError = null
        confirmPasswordError = null
        registroExitoso = false
    }
}