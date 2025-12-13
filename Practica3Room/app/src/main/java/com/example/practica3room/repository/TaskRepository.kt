package com.example.practica3room.repository

import android.util.Log
import com.example.practica3room.di.AppContainer
import com.example.practica3room.local.TaskDAO
import com.example.practica3room.local.TaskEntity
import com.example.practica3room.model.*
import com.example.practica3room.model.DateConverter.toEntity
import com.example.practica3room.model.DateConverter.toRequest
import com.example.practica3room.remote.TaskApiService
import com.example.practica3room.util.NetworkObserver
import com.example.practica3room.util.SyncPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TaskRepository(
    private val dao: TaskDAO,
    private val api: TaskApiService,
    private val prefs: SyncPrefs,
    private val networkObserver: NetworkObserver
) {
    companion object {
        private const val TAG = "TaskRepository"
    }

    // ← Observar tareas de un usuario específico
    fun observeTasksForUser(userId: Int): Flow<List<TaskEntity>> {
        Log.d(TAG, "👀 Observando tareas del usuario $userId")
        return dao.observeTasks(userId)
    }

    // ============ CREAR TAREA ============
    suspend fun addTask(
        name: String,
        deadline: String,
        status: Boolean = false,
        userId: Int  // ← REQUERIDO
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = AppContainer.currentUserId

        if (userId == -1) {
            return@withContext Result.failure(Exception("Usuario no definido"))
        }

        try {
            val task = TaskEntity(
                id = Random.nextInt(1, Int.MAX_VALUE),
                name = name,
                status = status,
                deadline = deadline,
                userId = userId,  // ← ASIGNAR userId
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )

            dao.insert(task)
            Log.d(TAG, "✅ Tarea guardada localmente para usuario $userId: ${task.name}")

            if (networkObserver.isConnected) {
                syncTaskToServer(task)
            } else {
                Log.d(TAG, "📴 Offline: Tarea quedará pendiente de sincronizar")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al agregar tarea", e)
            Result.failure(e)
        }
    }

    // ============ ACTUALIZAR TAREA ============
    suspend fun updateTask(
        id: Int,
        name: String,
        deadline: String,
        status: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existingTask = dao.getTaskById(id)
            if (existingTask == null) {
                return@withContext Result.failure(Exception("Tarea no encontrada"))
            }

            val updatedTask = existingTask.copy(
                name = name,
                deadline = deadline,
                status = status,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )

            dao.insert(updatedTask)
            Log.d(TAG, "✅ Tarea actualizada localmente: $name")

            if (networkObserver.isConnected) {
                syncTaskToServer(updatedTask)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar tarea", e)
            Result.failure(e)
        }
    }

    // ============ ACTUALIZAR SOLO ESTADO ============
    suspend fun updateTaskStatus(id: Int, newStatus: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val task = dao.getTaskById(id)
                    ?: return@withContext Result.failure(Exception("Tarea no encontrada"))

                val updatedTask = task.copy(
                    status = newStatus,
                    updatedAt = System.currentTimeMillis(),
                    pendingSync = true
                )

                dao.insert(updatedTask)

                if (networkObserver.isConnected) {
                    syncTaskToServer(updatedTask)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al actualizar estado", e)
                Result.failure(e)
            }
        }

    // ============ ELIMINAR TAREA ============
    suspend fun deleteTask(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val task = dao.getTaskById(id)
                ?: return@withContext Result.failure(Exception("Tarea no encontrada"))

            val deletedTask = task.copy(
                deleted = true,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )

            dao.insert(deletedTask)
            Log.d(TAG, "✅ Tarea marcada como eliminada: ${task.name}")

            if (networkObserver.isConnected) {
                deleteTaskFromServer(id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar tarea", e)
            Result.failure(e)
        }
    }

    // ============ SINCRONIZACIÓN ============
    suspend fun syncAll(userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!networkObserver.isConnected) {
                Log.w(TAG, "📴 Sin conexión a internet")
                return@withContext Result.failure(Exception("Sin conexión a internet"))
            }

            Log.d(TAG, "🔄 Iniciando sincronización para usuario $userId...")

            // 1. Subir tareas pendientes del usuario
            val pendingTasks = dao.getPendingSync(userId)
            Log.d(TAG, "📤 Tareas pendientes de subir: ${pendingTasks.size}")

            pendingTasks.forEach { task ->
                if (task.deleted) {
                    deleteTaskFromServer(task.id)
                } else {
                    syncTaskToServer(task)
                }
            }

            // 2. Descargar tareas del servidor
            val response = api.getTasks()
            if (response.isSuccessful) {
                val serverTasks = response.body() ?: emptyList()
                Log.d(TAG, "📥 Tareas del servidor: ${serverTasks.size}")

                // Filtrar solo las del usuario actual
                val userTasks = serverTasks.filter { it.userId == userId }
                Log.d(TAG, "📥 Tareas del usuario $userId: ${userTasks.size}")

                val entities = userTasks.map { it.toEntity() }
                dao.insertAll(entities)

                prefs.setLastSync(System.currentTimeMillis())
                Log.d(TAG, "✅ Sincronización completada para usuario $userId")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en sincronización", e)
            Result.failure(e)
        }
    }

    // ============ HELPERS PRIVADOS ============

    private suspend fun syncTaskToServer(task: TaskEntity) {
        try {
            val request = task.toRequest()

            val response = if (taskExistsOnServer(task.id)) {
                api.updateTask(task.id, request)
            } else {
                api.createTask(request)
            }

            if (response.isSuccessful) {
                dao.insert(task.copy(pendingSync = false))
                Log.d(TAG, "✅ Tarea sincronizada: ${task.name}")
            } else {
                Log.e(TAG, "❌ Error al sincronizar: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al sincronizar tarea", e)
        }
    }

    private suspend fun deleteTaskFromServer(taskId: Int) {
        try {
            val response = api.deleteTask(taskId)
            if (response.isSuccessful) {
                val task = dao.getTaskById(taskId)
                if (task != null) {
                    dao.delete(task)
                }
                Log.d(TAG, "✅ Tarea eliminada del servidor: $taskId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar del servidor", e)
        }
    }

    private suspend fun taskExistsOnServer(taskId: Int): Boolean {
        return try {
            val response = api.getTask(taskId)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllTasks(userId: Int): Result<List<TaskEntity>> = withContext(Dispatchers.IO) {
        try {
            val tasks = dao.getAllTasks(userId)
            Result.success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener tareas", e)
            Result.failure(e)
        }
    }
}