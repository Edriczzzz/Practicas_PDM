package com.example.practica3room.di

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.practica3room.local.AppDatabase
import com.example.practica3room.local.UserDao
import com.example.practica3room.repository.TaskRepository
import com.example.practica3room.remote.RetrofitClient
import com.example.practica3room.util.NetworkObserver
import com.example.practica3room.util.SyncPrefs
import com.example.practica3room.worker.TaskSyncWorker
import java.util.concurrent.TimeUnit



object AppContainer {

    private const val TAG = "AppContainer"

    private lateinit var database: AppDatabase
    private lateinit var syncPrefs: SyncPrefs
    private lateinit var networkObserver: NetworkObserver

    val isNetworkAvailable: Boolean
        get() = networkObserver.isConnected

    lateinit var taskRepository: TaskRepository
        private set

    var currentUserId: Int = -1
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
            currentUserId = syncPrefs.getUserId()
            isInitialized = true
            Log.d(TAG, "🎉 AppContainer inicializado completamente")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando AppContainer", e)
            throw e
        }
    }

    fun setCurrentUser(userId: Int) {
        currentUserId = userId
        Log.d(TAG, "👤 Usuario actual seteado: $userId")
        syncPrefs.saveUserId(userId)
    }

    fun clearCurrentUser() {
        currentUserId = -1
        Log.d(TAG, "👤 Usuario actual limpiado")
        syncPrefs.saveUserId(-1)
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException(
                "AppContainer no está inicializado. " +
                        "Llama a AppContainer.init(context) en MyApplication.onCreate()"
            )
        }
    }

    fun getSavedUserId(): Int {
        return syncPrefs.getUserId()
    }

    fun getUserDao(): UserDao = database.userDao()



    // Para testing o casos especiales
    fun reset() {
        isInitialized = false
        Log.d(TAG, "🔄 AppContainer reseteado")
    }
    fun scheduleSync(context: Context) {
        val userId = currentUserId
        if (userId == -1) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val req = PeriodicWorkRequestBuilder<TaskSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(workDataOf(TaskSyncWorker.KEY_USER_ID to userId))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "task_sync",
            ExistingPeriodicWorkPolicy.UPDATE, // o KEEP
            req
        )
    }


}