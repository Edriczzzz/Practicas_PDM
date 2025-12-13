package com.example.practica3room


import android.app.Application
import androidx.work.*
import com.example.practica3room.worker.TaskSyncWorker
import com.example.practica3room.di.AppContainer
import com.example.practica3room.util.SyncPrefs
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

        val userId = SyncPrefs(this).getUserId()

        if (userId == -1) {
            // No hay usuario → no tiene sentido sincronizar
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<TaskSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    TaskSyncWorker.KEY_USER_ID to userId
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "task_sync",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
    }

}


