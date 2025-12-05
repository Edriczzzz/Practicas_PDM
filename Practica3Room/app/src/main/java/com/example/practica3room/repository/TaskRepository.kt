package com.example.practica3room.data.repository

import android.util.Log
import com.example.practica3room.local.TaskDAO
import com.example.practica3room.local.TaskEntity
import com.example.practica3room.model.*
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

    // Observar tareas desde Room (fuente única de verdad)
    val tasks: Flow<List<TaskEntity>> = dao.observeTasks()

    // ============ CREAR TAREA ============
    suspend fun addTask(name: String, deadline: String, status: Boolean = false): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val task = TaskEntity(
                    id = Random.nextInt(1, Int.MAX_VALUE),
                    name = name,
                    status = status,
                    deadline = deadline,
                    updatedAt = System.currentTimeMillis(),
                    pendingSync = true  // Marcar como pendiente de sincronizar
                )

                // Guardar local primero
                dao.insert(task)
                Log.d(TAG, "✅ Tarea guardada localmente: ${task.name}")

                // Si hay internet, sincronizar inmediatamente
                if (networkObserver.isConnected) {
                    syncTaskToServer(task)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al agregar tarea", e)
                Result.failure(e)
            }
        }

    // ============ ACTUALIZAR TAREA ============
    suspend fun updateTask(id: Int, name: String, deadline: String, status: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
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

                // Si hay internet, sincronizar inmediatamente
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
                val task = dao.getTaskById(id) ?: return@withContext Result.failure(
                    Exception("Tarea no encontrada")
                )

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
            val task = dao.getTaskById(id) ?: return@withContext Result.failure(
                Exception("Tarea no encontrada")
            )

            // Soft delete: solo marcar como eliminado
            val deletedTask = task.copy(
                deleted = true,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )

            dao.insert(deletedTask)
            Log.d(TAG, "✅ Tarea marcada como eliminada: ${task.name}")

            // Si hay internet, eliminar del servidor
            if (networkObserver.isConnected) {
                deleteTaskFromServer(id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar tarea", e)
            Result.failure(e)
        }
    }

    // ============ SINCRONIZACIÓN CON SERVIDOR ============
    suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!networkObserver.isConnected) {
                return@withContext Result.failure(Exception("Sin conexión a internet"))
            }

            Log.d(TAG, "🔄 Iniciando sincronización completa...")

            // 1. Obtener tareas pendientes de sincronizar
            val pendingTasks = dao.getPendingSync()
            Log.d(TAG, "📤 Tareas pendientes de subir: ${pendingTasks.size}")

            // 2. Sincronizar cada tarea pendiente con el servidor
            pendingTasks.forEach { task ->
                if (task.deleted) {
                    deleteTaskFromServer(task.id)
                } else {
                    syncTaskToServer(task)
                }
            }

            // 3. Descargar todas las tareas del servidor
            val response = api.getTasks()
            if (response.isSuccessful) {
                val serverTasks = response.body() ?: emptyList()
                Log.d(TAG, "📥 Tareas del servidor: ${serverTasks.size}")

                // Convertir y guardar localmente
                val entities = serverTasks.map { it.toEntity() }
                dao.insertAll(entities)

                prefs.setLastSync(System.currentTimeMillis())
                Log.d(TAG, "✅ Sincronización completada")
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

            val response = if (task.id > 0 && taskExistsOnServer(task.id)) {
                // Actualizar tarea existente
                api.updateTask(task.id, request)
            } else {
                // Crear nueva tarea
                api.createTask(request)
            }

            if (response.isSuccessful) {
                // Marcar como sincronizado
                dao.insert(task.copy(pendingSync = false))
                Log.d(TAG, "✅ Tarea sincronizada con servidor: ${task.name}")
            } else {
                Log.e(TAG, "❌ Error al sincronizar tarea: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al sincronizar tarea", e)
        }
    }

    private suspend fun deleteTaskFromServer(taskId: Int) {
        try {
            val response = api.deleteTask(taskId)
            if (response.isSuccessful) {
                // Eliminar físicamente de Room después de eliminar del servidor
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

    // ============ OBTENER TODAS LAS TAREAS (para pantallas) ============
    suspend fun getAllTasks(): Result<List<TaskEntity>> = withContext(Dispatchers.IO) {
        try {
            val tasks = dao.getAllTasks()
            Result.success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener tareas", e)
            Result.failure(e)
        }
    }
}