package com.example.practica3room.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Task")
data class Task (
        @PrimaryKey (autoGenerate = true) val  id: Int = 0,
        val name: String,
        val plannedD: String,
        val status: Boolean
)