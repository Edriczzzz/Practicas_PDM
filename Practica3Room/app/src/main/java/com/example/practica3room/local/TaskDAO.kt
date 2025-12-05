package com.example.practica3room.local


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDAO {

    // Observar todas las tareas no eliminadas (Flow para actualizaciones automáticas)
    @Query("SELECT * FROM tasks WHERE deleted = 0 ORDER BY deadline ASC")
    fun observeTasks(): Flow<List<TaskEntity>>

    // Obtener todas las tareas (sin Flow, para uso directo)
    @Query("SELECT * FROM tasks WHERE deleted = 0 ORDER BY deadline ASC")
    suspend fun getAllTasks(): List<TaskEntity>

    // Obtener una tarea por ID
    @Query("SELECT * FROM tasks WHERE id = :id AND deleted = 0")
    suspend fun getTaskById(id: Int): TaskEntity?

    // Insertar o actualizar tarea (REPLACE si ya existe)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    // Insertar múltiples tareas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    // Actualizar tarea existente
    @Update
    suspend fun update(task: TaskEntity)

    // Eliminar tarea físicamente de la base de datos
    @Delete
    suspend fun delete(task: TaskEntity)

    // Obtener tareas pendientes de sincronización con el servidor
    @Query("SELECT * FROM tasks WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<TaskEntity>

    // Marcar tareas como sincronizadas (pendingSync = false)
    @Query("UPDATE tasks SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun clearPending(ids: List<Int>)

    // Contar cuántas tareas hay en total
    @Query("SELECT COUNT(*) FROM tasks WHERE deleted = 0")
    suspend fun getTaskCount(): Int

    // Obtener tareas por estado
    @Query("SELECT * FROM tasks WHERE status = :status AND deleted = 0 ORDER BY deadline ASC")
    suspend fun getTasksByStatus(status: Boolean): List<TaskEntity>

    // Obtener tareas pendientes (no completadas)
    @Query("SELECT * FROM tasks WHERE status = 0 AND deleted = 0 ORDER BY deadline ASC")
    fun observePendingTasks(): Flow<List<TaskEntity>>

    // Obtener tareas completadas
    @Query("SELECT * FROM tasks WHERE status = 1 AND deleted = 0 ORDER BY deadline DESC")
    fun observeCompletedTasks(): Flow<List<TaskEntity>>

    // Limpiar todas las tareas (útil para testing o reset)
    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    // Eliminar físicamente las tareas marcadas como deleted
    @Query("DELETE FROM tasks WHERE deleted = 1")
    suspend fun purgeDeleted()
}