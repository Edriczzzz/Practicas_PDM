package com.example.practica3room.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica3room.di.AppContainer
import com.example.practica3room.local.TaskEntity
import com.example.practica3room.repository.TaskApiRepository
import com.example.practica3room.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job


// ---------------- UI STATE ----------------
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class TaskViewModel(
    private val repository: TaskRepository,
    private val apiRepository: TaskApiRepository
) : ViewModel() {

    companion object {
        private const val TAG = "TaskViewModel"
    }

    // ---------------- STATES ----------------
    private val _tasksState = MutableStateFlow<UiState<List<TaskEntity>>>(UiState.Idle)
    val tasksState: StateFlow<UiState<List<TaskEntity>>> = _tasksState.asStateFlow()

    private val _authState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val authState: StateFlow<UiState<String>> = _authState.asStateFlow()

    private val _operationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val operationState: StateFlow<UiState<String>> = _operationState.asStateFlow()

    private val _syncState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val syncState: StateFlow<UiState<String>> = _syncState.asStateFlow()


    private var observeTasksJob: Job? = null



    // ---------------- LOGIN ----------------
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
// 🔹 CASO OFFLINE → validar contra Room
            if (!AppContainer.isNetworkAvailable) {
                val userDao = AppContainer.getUserDao()
                val user = userDao.loginOffline(username, password)
                val users = userDao.getAll()
                Log.d(TAG, "👥 Usuarios en Room: $users")

                if (user != null) {
                    AppContainer.setCurrentUser(user.id)
                    startObservingUserTasks(user.id)
                    _authState.value = UiState.Success("Offline")
                } else {
                    _authState.value = UiState.Error("Usuario o contraseña incorrectos (offline)")
                }
                return@launch
            }



            // 🔹 CASO 2: CON CONEXIÓN → LOGIN NORMAL
            val result = apiRepository.login(username, password)

            if (result.isSuccess) {
                val userId = result.getOrNull()!!

                AppContainer.setCurrentUser(userId)
                startObservingUserTasks(userId)
                syncAll()

                _authState.value = UiState.Success("Login correcto")
            } else {
                _authState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Login fallido"
                )
            }
        }
    }



    fun logout() {
        viewModelScope.launch {
            apiRepository.logout()
            AppContainer.clearCurrentUser()

            observeTasksJob?.cancel()
            observeTasksJob = null

            _authState.value = UiState.Idle
            _tasksState.value = UiState.Idle
        }
    }


    fun enterOfflineMode() {
        val userId = AppContainer.currentUserId
        if (userId != -1) {
            startObservingUserTasks(userId)
            _authState.value = UiState.Success("Offline")
        } else {
            _authState.value = UiState.Error("No hay usuario guardado")
        }
    }

    // ---------------- TASK OBSERVER ----------------
    private fun startObservingUserTasks(userId: Int) {
        // ✅ Cancela el observer anterior para que NO haya 2 collectors activos
        observeTasksJob?.cancel()

        observeTasksJob = viewModelScope.launch {
            _tasksState.value = UiState.Loading

            repository.observeTasksForUser(userId)
                .catch { e ->
                    Log.e(TAG, "❌ Error observando tareas", e)
                    _tasksState.value = UiState.Error(e.message ?: "Error al cargar tareas")
                }
                .collect { tasks ->
                    Log.d(TAG, "📋 ${tasks.size} tareas para userId=$userId")
                    _tasksState.value = UiState.Success(tasks)
                }
        }
    }



    // ---------------- CRUD ----------------
    fun createTask(name: String, deadline: String) {
        viewModelScope.launch {
            val userId = AppContainer.currentUserId
            if (userId == -1) {
                _operationState.value = UiState.Error("Usuario no autenticado")
                return@launch
            }

            _operationState.value = UiState.Loading

            val result = repository.addTask(
                name = name,
                deadline = deadline,
                status = false,
                userId = userId
            )

            _operationState.value = if (result.isSuccess) {
                UiState.Success("Tarea creada")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al crear")
            }
        }
    }

    fun updateTask(id: Int, name: String, deadline: String, status: Boolean) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = repository.updateTask(id, name, deadline, status)

            _operationState.value = if (result.isSuccess) {
                UiState.Success("Tarea actualizada")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = repository.deleteTask(id)

            _operationState.value = if (result.isSuccess) {
                UiState.Success("Tarea eliminada")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }

    // ---------------- SYNC ----------------
    fun syncAll() {
        viewModelScope.launch {
            val userId = AppContainer.currentUserId
            if (userId == -1) return@launch

            _syncState.value = UiState.Loading

            val result = repository.syncAll(userId)

            _syncState.value = if (result.isSuccess) {
                UiState.Success("Sync OK")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error en sync")
            }
        }
    }

    // ---------------- HELPERS ----------------
    fun getTaskById(localId: Int): TaskEntity? {
        return (tasksState.value as? UiState.Success)
            ?.data
            ?.find { it.localId == localId }
    }

    fun resetOperationState() {
        _operationState.value = UiState.Idle
    }
    fun loadTasks() {
        val userId = AppContainer.currentUserId
        if (userId == -1) {
            _tasksState.value = UiState.Error("Usuario no autenticado")
            return
        }

        // El Flow ya está observando Room, solo forzamos sync
        syncAll()
    }

    fun resetAuthState() {
        _authState.value = UiState.Idle
    }
    fun updateTaskStatus(id: Int, newStatus: Boolean) {
        viewModelScope.launch {
            val result = repository.updateTaskStatus(id, newStatus)

            if (result.isFailure) {
                Log.e(
                    "TaskViewModel",
                    "❌ Error actualizando estado: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }


}
