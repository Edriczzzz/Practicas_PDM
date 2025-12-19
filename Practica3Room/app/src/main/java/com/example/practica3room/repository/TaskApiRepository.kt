package com.example.practica3room.repository

import android.util.Log
import com.example.practica3room.di.AppContainer
import com.example.practica3room.local.UserEntity
import com.example.practica3room.model.*
import com.example.practica3room.remote.RetrofitClient
import com.example.practica3room.util.SyncPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskApiRepository {

    private val authService = RetrofitClient.authService
    private val taskService = RetrofitClient.taskService

    companion object {
        private const val TAG = "TaskApiRepository"
    }

    // ============ AUTENTICACIÓN ============


    suspend fun login(username: String, password: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = authService.login(LoginRequest(username, password))

                if (!response.isSuccessful || response.body() == null) {
                    return@withContext Result.failure(Exception("Login inválido"))
                }

                val body = response.body()!!

                val user = body.user
                    ?: return@withContext Result.failure(Exception("Usuario nulo en login"))

                val token = body.token
                val userId = user.id

                // Guardar token global
                RetrofitClient.setAuthToken(token)

                // Guardar usuario ACTIVO
                AppContainer.setCurrentUser(userId)


                val userEntity = UserEntity(
                    id = userId,
                    username = username,
                    password = password
                )
                AppContainer.getUserDao().insert(userEntity)


                Log.d(TAG, "✅ Login correcto userId=$userId")
                Result.success(userId)



            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en login", e)
                Result.failure(e)
            }
        }


    suspend fun logout() {
        RetrofitClient.setAuthToken(null)
        Log.d(TAG, "🔓 Sesión cerrada")
    }

    // ============ OPERACIONES DE TAREAS (solo para uso interno) ============

    suspend fun getAllTasks(): Result<List<TaskApi>> = withContext(Dispatchers.IO) {
        try {
            val response = taskService.getTasks()

            if (response.isSuccessful) {
                val tasks = response.body() ?: emptyList()
                Log.d(TAG, "✅ Tareas obtenidas: ${tasks.size}")
                Result.success(tasks)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception", e)
            Result.failure(e)
        }
    }

    suspend fun getTaskById(id: Int): Result<TaskApi> = withContext(Dispatchers.IO) {
        try {
            val response = taskService.getTask(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Tarea no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTask(
        name: String,
        deadline: String,
        status: Boolean = false,
        userId: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val apiDeadline = DateConverter.toApiFormat(deadline)

            val request = TaskRequest(
                name = name,
                status = status,
                deadline = apiDeadline,
                userId = userId  // ← INCLUIR userId
            )

            val response = taskService.createTask(request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Tarea creada para usuario $userId")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(
        id: Int,
        name: String,
        deadline: String,
        status: Boolean,
        userId: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiDeadline = DateConverter.toApiFormat(deadline)

            val request = TaskRequest(
                name = name,
                status = status,
                deadline = apiDeadline,
                userId = userId  // ← INCLUIR userId
            )

            val response = taskService.updateTask(id, request)

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Tarea actualizada")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(id: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = taskService.deleteTask(id)

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Tarea eliminada")
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ← DATA CLASS para devolver datos del login
data class LoginData(
    val token: String,
    val userId: Int
)