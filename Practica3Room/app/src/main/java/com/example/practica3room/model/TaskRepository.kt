package com.example.practica3room.model

import com.example.practica3room.model.Task
import com.example.practica3room.model.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    suspend fun insert(task: Task) = dao.insertTask(task)

    fun getAll(): Flow<List<Task>> = dao.getAllTasks()

    suspend fun findById(taskId: Int): Task? = dao.getTaskById(taskId)
    
    suspend fun update(task: Task) = dao.updateTask(task)

    suspend fun deleteById(taskId: Int) = dao.deleteTaskById(taskId)

    suspend fun deleteAll() = dao.deleteAllTasks()

    suspend fun markAsCompleted(taskId: Int) = dao.markTaskAsCompleted(taskId)

    suspend fun updateStatus(taskId: Int, newStatus: Boolean) = dao.updateTaskStatus(taskId, newStatus)

}