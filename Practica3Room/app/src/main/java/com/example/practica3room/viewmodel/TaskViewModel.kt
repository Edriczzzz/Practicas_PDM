package com.example.practica3room.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica3room.local.TaskEntity
import com.example.practica3room.repository.TaskRepository
import com.example.practica3room.repository.TaskApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estados de la UI
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class TaskViewModel(
    private val repository: TaskRepository,
    private val apiRepository: TaskApiRepository  // Mantener para login
) : ViewModel() {

    companion object {
        private const val TAG = "TaskViewModel"
    }

    // Estado de las tareas desde Room (Flow automático)
    val tasksState: StateFlow<UiState<List<TaskEntity>>> = repository.tasks
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        .let { flow ->
            MutableStateFlow<UiState<List<TaskEntity>>>(UiState.Loading).apply {
                viewModelScope.launch {
                    flow.collect { tasks ->
                        value = UiState.Success(tasks)
                    }
                }
            }
        }

    // Estado de operaciones individuales (crear, actualizar, eliminar)
    private val _operationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val operationState: StateFlow<UiState<String>> = _operationState.asStateFlow()

    // Estado de autenticación
    private val _authState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val authState: StateFlow<UiState<String>> = _authState.asStateFlow()

    // Estado de sincronización
    private val _syncState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val syncState: StateFlow<UiState<String>> = _syncState.asStateFlow()

    // ============ AUTENTICACIÓN (usa API directamente) ============

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = apiRepository.login(username, password)

            _authState.value = if (result.isSuccess) {
                // Después del login exitoso, sincronizar tareas
                syncAll()
                UiState.Success("Login exitoso")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            apiRepository.logout()
            _authState.value = UiState.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = UiState.Idle
    }

    // ============ OPERACIONES DE TAREAS (usan Repository híbrido) ============

    fun loadTasks() {
        viewModelScope.launch {
            Log.d(TAG, "📂 Cargando tareas locales...")
            // Las tareas se cargan automáticamente del Flow, pero forzamos sincronización
            syncAll()
        }
    }

    fun createTask(name: String, deadline: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            Log.d(TAG, "➕ Creando tarea: $name")
            val result = repository.addTask(name, deadline, false)

            _operationState.value = if (result.isSuccess) {
                UiState.Success("Tarea creada exitosamente")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al crear tarea")
            }
        }
    }

    fun updateTask(id: Int, name: String, deadline: String, status: Boolean) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            Log.d(TAG, "✏️ Actualizando tarea $id: $name")
            val result = repository.updateTask(id, name, deadline, status)

            _operationState.value = if (result.isSuccess) {
                UiState.Success("Tarea actualizada exitosamente")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            Log.d(TAG, "🗑️ Eliminando tarea $id")
            val result = repository.deleteTask(id)

            _operationState.value = if (result.isSuccess) {
                UiState.Success("Tarea eliminada exitosamente")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }

    fun updateTaskStatus(id: Int, newStatus: Boolean) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Actualizando estado de tarea $id a: $newStatus")
            repository.updateTaskStatus(id, newStatus)
        }
    }

    // ============ SINCRONIZACIÓN ============

    fun syncAll() {
        viewModelScope.launch {
            _syncState.value = UiState.Loading

            Log.d(TAG, "🔄 Sincronizando con servidor...")
            val result = repository.syncAll()

            _syncState.value = if (result.isSuccess) {
                Log.d(TAG, "✅ Sincronización completada")
                UiState.Success("Sincronización exitosa")
            } else {
                Log.e(TAG, "❌ Error en sincronización: ${result.exceptionOrNull()?.message}")
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al sincronizar")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = UiState.Idle
    }

    fun resetSyncState() {
        _syncState.value = UiState.Idle
    }

    // ============ HELPER ============

    // Obtener una tarea específica del estado actual
    fun getTaskById(id: Int): TaskEntity? {
        return when (val state = tasksState.value) {
            is UiState.Success -> state.data.find { it.id == id }
            else -> null
        }
    }
}