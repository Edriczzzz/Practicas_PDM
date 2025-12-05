package com.example.practica3room.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: Boolean,
    val deadline: String,  // Formato: yyyy-MM-dd
    val updatedAt: Long,
    val pendingSync: Boolean = false,  // ¿Necesita sincronizarse con el servidor?
    val deleted: Boolean = false       // Marcado como eliminado (soft delete)
)