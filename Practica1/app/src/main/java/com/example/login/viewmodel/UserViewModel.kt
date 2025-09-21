package com.example.login.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login.model.LoginState
import com.example.login.model.UserModel
import com.example.login.model.ValidationResponse
import com.example.login.provider.UserProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserViewModel(private val userProvider: UserProvider) : ViewModel() {

    // LiveData para el estado del login
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: MutableLiveData<LoginState> = _loginState

    // LiveData para validaciones
    private val _validationResult = MutableLiveData<ValidationResponse>()
    val validationResult: MutableLiveData<ValidationResponse> = _validationResult

    // Usuario actual logueado
    private val _currentUser = MutableLiveData<UserModel>()
    val currentUser: MutableLiveData<UserModel> = _currentUser

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

    // Validar recuperación de contraseña
    fun validateRecovery(email: String, secretAnswer: String) {
        viewModelScope.launch {
            val result = userProvider.validateRecovery(email, secretAnswer)
            _validationResult.value = result
        }
    }

    // Cambiar contraseña (desde menú)
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

    // Restablecer contraseña (después de recovery)
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
        _loginState.value = LoginState()
        _validationResult.value = ValidationResponse(true)
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