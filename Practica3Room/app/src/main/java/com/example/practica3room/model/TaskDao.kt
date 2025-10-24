package com.example.practica3room.model
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM task")
    suspend fun mostrarTask(id: Int): Task?

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM Task WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("DELETE FROM Task")
    suspend fun deleteAllTasks()

    @Query("SELECT * FROM Task WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("SELECT * FROM Task ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("UPDATE Task SET status = 1 WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Int)

}