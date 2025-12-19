package com.example.practica3room.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,

    val id: Int? = null, // remote id (nullable hasta que se cree en server)

    val name: String,
    val status: Boolean,
    val deadline: String,
    val userId: Int,
    val updatedAt: Long,
    val pendingSync: Boolean = false,
    val deleted: Boolean = false
)
