package com.example.practica3room.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val username: String,
    val password: String // ⚠️ idealmente hash, pero usamos texto si tu API así trabaja
)
