package com.example.practica3room.repository

import android.util.Log
import com.example.practica3room.model.*
import com.example.practica3room.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class TaskApiRepository {

    private val authService = RetrofitClient.authService
    private val taskService = RetrofitClient.taskService

    companion object {
        private const val TAG = "TaskApiRepository"
    }

    // ============ AUTENTICACIÓN ============

    suspend fun login(username: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(username, password)
            val response = authService.login(request)

            if (response.isSuccessful) {
                val token = response.body()?.token
                    ?: return@withContext Result.failure(Exception("Token null en respuesta"))

                // Configurar token globalmente en RetrofitClient
                RetrofitClient.setAuthToken(token)
                Log.d(TAG, "✅ Login exitoso, token configurado")
                Result.success(token)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Login fallido: HTTP ${response.code()} - $errorBody")
                Result.failure(Exception("Login error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception en login", e)
            Result.failure(e)
        }
    }

    suspend fun logout() {
        RetrofitClient.setAuthToken(null)
        Log.d(TAG, "🔓 Sesión cerrada, token eliminado")
    }

    // ============ OPERACIONES DE TAREAS ============

    suspend fun getAllTasks(): Result<List<TaskApi>> = withContext(Dispatchers.IO) {
        try {

            // Ya no es necesario pasar el token manualmente
            val response: Response<List<TaskApi>> = taskService.getTasks()

            if (response.isSuccessful) {
                val tasks = response.body() ?: emptyList()
                Log.d(TAG, "✅ Tareas obtenidas: ${tasks.size}")
                Result.success(tasks)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al obtener tareas: HTTP ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al obtener tareas", e)
            Result.failure(e)
        }
    }

    suspend fun getTaskById(id: Int): Result<TaskApi> = withContext(Dispatchers.IO) {
        try {
            val response = taskService.getTask(id)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✅ Tarea $id obtenida")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Tarea $id no encontrada: ${response.code()}")
                Result.failure(Exception("Tarea no encontrada: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al obtener tarea $id", e)
            Result.failure(e)
        }
    }

    suspend fun createTask(name: String, deadline: String, status: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Convertir fecha de dd/MM/yyyy a yyyy-MM-dd
            val apiDeadline = DateConverter.toApiFormat(deadline)

            val request = TaskRequest(
                name = name,
                status = status,
                deadline = apiDeadline
            )

            val response = taskService.createTask(request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Tarea creada: $name")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al crear tarea: HTTP ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al crear tarea", e)
            Result.failure(e)
        }
    }

    suspend fun updateTask(id: Int, name: String, deadline: String, status: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Convertir fecha de dd/MM/yyyy a yyyy-MM-dd
            val apiDeadline = DateConverter.toApiFormat(deadline)

            val request = TaskRequest(
                name = name,
                status = status,
                deadline = apiDeadline
            )

            val response = taskService.updateTask(id, request)

            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Tarea actualizada"
                Log.d(TAG, "✅ Tarea $id actualizada")
                Result.success(message)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al actualizar tarea $id: ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al actualizar tarea $id", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTask(id: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = taskService.deleteTask(id)

            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Tarea eliminada"
                Log.d(TAG, "✅ Tarea $id eliminada")
                Result.success(message)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al eliminar tarea $id: ${response.code()}")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al eliminar tarea $id", e)
            Result.failure(e)
        }
    }

    suspend fun updateTaskStatus(id: Int, newStatus: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Primero obtenemos la tarea para mantener los otros datos
            val taskResult = getTaskById(id)
            if (taskResult.isFailure) {
                return@withContext Result.failure(taskResult.exceptionOrNull()!!)
            }

            val task = taskResult.getOrNull()!!

            // Actualizamos con el nuevo estado
            updateTask(
                id = id,
                name = task.name,
                deadline = task.deadline,
                status = newStatus
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al actualizar estado de tarea $id", e)
            Result.failure(e)
        }
    }
}