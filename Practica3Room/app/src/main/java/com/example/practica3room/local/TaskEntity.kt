package com.example.practica3room.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: Boolean,
    val deadline: String,
    val userId: Int,  // ← AGREGAR ESTO
    val updatedAt: Long,
    val pendingSync: Boolean = false,
    val deleted: Boolean = false
)