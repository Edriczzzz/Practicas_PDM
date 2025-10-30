package com.example.practica3room.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica3room.model.Task
import com.example.practica3room.model.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // Flow de todas las tareas
    val allTasks: Flow<List<Task>> = repository.getAll()

    // Insertar una tarea
    fun insertTask(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    // Actualizar una tarea
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    // Eliminar una tarea por ID
    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteById(taskId)
        }
    }

    // Eliminar todas las tareas
    fun deleteAllTasks() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // Marcar tarea como completada
    fun markTaskAsCompleted(taskId: Int) {
        viewModelScope.launch {
            repository.markAsCompleted(taskId)
        }
    }

    // Obtener tarea por ID
    suspend fun getTaskById(taskId: Int): Task? {
        return repository.findById(taskId)
    }

    // Actualizar el estado de una tarea
    fun updateTaskStatus(taskId: Int, newStatus: Boolean) {
        viewModelScope.launch {
            repository.updateStatus(taskId, newStatus)
        }
    }
}