package com.example.login.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login.model.LoginState
import com.example.login.model.UserModel
import com.example.login.model.ValidationResponse
import com.example.login.model.RecoveryResponse
import com.example.login.provider.UserProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserViewModel(private val userProvider: UserProvider) : ViewModel() {

    // LiveData para el estado del login
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: MutableLiveData<LoginState> = _loginState

    // LiveData para validaciones generales
    private val _validationResult = MutableLiveData<ValidationResponse>()
    val validationResult: MutableLiveData<ValidationResponse> = _validationResult

    // LiveData específico para recovery (incluye pregunta secreta)
    private val _recoveryResult = MutableLiveData<RecoveryResponse>()
    val recoveryResult: MutableLiveData<RecoveryResponse> = _recoveryResult

    // Usuario actual logueado
    private val _currentUser = MutableLiveData<UserModel>()
    val currentUser: MutableLiveData<UserModel> = _currentUser

    // Estado de loading para recovery
    private val _isRecoveryLoading = MutableLiveData<Boolean>()
    val isRecoveryLoading: MutableLiveData<Boolean> = _isRecoveryLoading

    // Realizar login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            // Simular delay de red
            delay(1500)

            val result = userProvider.validateLogin(email, password)

            if (result.isValid) {
                val user = userProvider.getUserByEmail(email)
                _currentUser.value = user
                _loginState.value = LoginState(
                    isLoading = false,
                    isSuccess = true,
                    user = user
                )
            } else {
                _loginState.value = LoginState(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = result.message
                )
            }
        }
    }

    // Primer paso del recovery: obtener pregunta secreta por email
    fun getSecretQuestion(email: String) {
        viewModelScope.launch {
            _isRecoveryLoading.value = true

            // Simular delay de red
            delay(800)

            val result = userProvider.getSecretQuestion(email)
            _recoveryResult.value = result
            _isRecoveryLoading.value = false
        }
    }

    // Segundo paso del recovery: validar respuesta secreta
    fun validateRecovery(email: String, secretAnswer: String) {
        viewModelScope.launch {
            _isRecoveryLoading.value = true

            // Simular delay de red
            delay(1000)

            val result = userProvider.validateRecovery(email, secretAnswer)
            _validationResult.value = result
            _isRecoveryLoading.value = false
        }
    }

    // Cambiar contraseña (desde menú del usuario logueado)
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (newPassword != confirmPassword) {
                _validationResult.value = ValidationResponse(false, "Las contraseñas no coinciden")
                return@launch
            }

            val currentUserEmail = _currentUser.value?.email
            if (currentUserEmail != null) {
                val result = userProvider.changePassword(currentUserEmail, currentPassword, newPassword)
                _validationResult.value = result

                if (result.isValid) {
                    // Actualizar el usuario actual con la nueva contraseña
                    val updatedUser = _currentUser.value?.copy(passwd = newPassword)
                    _currentUser.value = updatedUser
                }
            } else {
                _validationResult.value = ValidationResponse(false, "No hay usuario logueado")
            }
        }
    }

    // Restablecer contraseña después de recovery
    fun resetPassword(email: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (newPassword != confirmPassword) {
                _validationResult.value = ValidationResponse(false, "Las contraseñas no coinciden")
                return@launch
            }

            val result = userProvider.resetPassword(email, newPassword)
            _validationResult.value = result
        }
    }

    // Logout
    fun logout() {
        _currentUser.value = null
        _loginState.value = LoginState()
    }

    // Limpiar estados
    fun clearStates() {
        println("DEBUG: UserViewModel clearStates called")
        _loginState.value = LoginState()
        _isRecoveryLoading.value = false
        // No limpiar validation ni recovery result automáticamente
    }

    // Limpiar específicamente los resultados de recovery
    fun clearRecoveryResults() {
        _recoveryResult.value = RecoveryResponse(false, "")
        _validationResult.value = ValidationResponse(false, "")
    }

    // Validar email en tiempo real
    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }

    // Validar contraseña en tiempo real
    fun validatePasswordFormat(password: String): ValidationResponse {
        if (password.length < 8) {
            return ValidationResponse(false, "Mínimo 8 caracteres")
        }

        if (!password.any { it.isUpperCase() }) {
            return ValidationResponse(false, "Debe contener al menos una mayúscula")
        }

        if (!password.any { it.isDigit() }) {
            return ValidationResponse(false, "Debe contener al menos un número")
        }

        if (!password.any { it in "_.$#?" }) {
            return ValidationResponse(false, "Debe contener al menos un símbolo (_, ., #, $, ?)")
        }

        return ValidationResponse(true, "Contraseña válida")
    }
}