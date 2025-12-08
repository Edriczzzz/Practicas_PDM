package com.example.practica3room.di

import android.content.Context
import android.util.Log
import com.example.practica3room.local.AppDatabase
import com.example.practica3room.repository.TaskRepository
import com.example.practica3room.remote.RetrofitClient
import com.example.practica3room.util.NetworkObserver
import com.example.practica3room.util.SyncPrefs

object AppContainer {

    private const val TAG = "AppContainer"

    private lateinit var database: AppDatabase
    private lateinit var syncPrefs: SyncPrefs
    private lateinit var networkObserver: NetworkObserver

    val isNetworkAvailable: Boolean
        get() = networkObserver.isConnected


    lateinit var taskRepository: TaskRepository
        private set

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) {
            Log.w(TAG, "⚠️ AppContainer ya está inicializado")
            return
        }

        try {
            Log.d(TAG, "🚀 Inicializando AppContainer...")

            // Inicializar base de datos Room
            database = AppDatabase.get(context)
            Log.d(TAG, "✅ Base de datos Room inicializada")

            // Inicializar utilidades
            syncPrefs = SyncPrefs(context)
            Log.d(TAG, "✅ SyncPrefs inicializado")

            networkObserver = NetworkObserver(context)
            Log.d(TAG, "✅ NetworkObserver inicializado - Conectado: ${networkObserver.isConnected}")

            // Inicializar Repository con todas las dependencias
            taskRepository = TaskRepository(
                dao = database.taskDao(),
                api = RetrofitClient.taskService,
                prefs = syncPrefs,
                networkObserver = networkObserver
            )
            Log.d(TAG, "✅ TaskRepository inicializado")

            isInitialized = true
            Log.d(TAG, "🎉 AppContainer inicializado completamente")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando AppContainer", e)
            throw e
        }
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException(
                "AppContainer no está inicializado. " +
                        "Llama a AppContainer.init(context) en MyApplication.onCreate()"
            )
        }
    }

    // Para testing o casos especiales
    fun reset() {
        isInitialized = false
        Log.d(TAG, "🔄 AppContainer reseteado")
    }
}