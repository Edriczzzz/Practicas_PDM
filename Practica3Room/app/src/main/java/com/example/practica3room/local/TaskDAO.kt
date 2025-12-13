package com.example.practica3room.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDAO {

    // Observar tareas de un usuario específico
    @Query("SELECT * FROM tasks WHERE deleted = 0 AND userId = :userId ORDER BY deadline ASC")
    fun observeTasks(userId: Int): Flow<List<TaskEntity>>

    // Obtener todas las tareas de un usuario
    @Query("SELECT * FROM tasks WHERE deleted = 0 AND userId = :userId ORDER BY deadline ASC")
    suspend fun getAllTasks(userId: Int): List<TaskEntity>

    // Obtener una tarea por ID (sin filtro de usuario para operaciones internas)
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    // Insertar o actualizar tarea
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    // Insertar múltiples tareas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    // Actualizar tarea
    @Update
    suspend fun update(task: TaskEntity)

    // Eliminar tarea físicamente
    @Delete
    suspend fun delete(task: TaskEntity)

    // Obtener tareas pendientes de sincronización de un usuario
    @Query("SELECT * FROM tasks WHERE pendingSync = 1 AND userId = :userId")
    suspend fun getPendingSync(userId: Int): List<TaskEntity>

    // Marcar tareas como sincronizadas
    @Query("UPDATE tasks SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun clearPending(ids: List<Int>)

    // Contar tareas de un usuario
    @Query("SELECT COUNT(*) FROM tasks WHERE deleted = 0 AND userId = :userId")
    suspend fun getTaskCount(userId: Int): Int

    // Obtener tareas por estado de un usuario
    @Query("SELECT * FROM tasks WHERE status = :status AND deleted = 0 AND userId = :userId ORDER BY deadline ASC")
    suspend fun getTasksByStatus(status: Boolean, userId: Int): List<TaskEntity>

    // Limpiar todas las tareas (testing)
    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    // Eliminar físicamente tareas marcadas como deleted
    @Query("DELETE FROM tasks WHERE deleted = 1")
    suspend fun purgeDeleted()
}