package edu.ipn.upiita.pdm.navjpc.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Usuario::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
} 