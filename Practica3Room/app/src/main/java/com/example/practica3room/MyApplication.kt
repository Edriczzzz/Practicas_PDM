package com.example.practica3room


import android.app.Application
import androidx.work.*
import com.example.practica3room.worker.TaskSyncWorker
import com.example.practica3room.di.AppContainer
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar AppContainer
        AppContainer.init(this)

        // Configurar sincronización periódica
        setupPeriodicSync()
    }

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<TaskSyncWorker>(
            15, TimeUnit.MINUTES  // Sincronizar cada 15 minutos
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "task_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}