package com.example.practica3room.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    // Guardar token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    // Obtener token como Flow
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // Obtener token de forma suspendida (para uso directo)
    suspend fun getToken(): String? {
        var tokenValue: String? = null
        context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }.collect { token ->
            tokenValue = token
        }
        return tokenValue
    }

    // Borrar token (logout)
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    // Formatear token para header Authorization
    fun formatToken(token: String?): String {
        return if (token != null) "Bearer $token" else ""
    }
}