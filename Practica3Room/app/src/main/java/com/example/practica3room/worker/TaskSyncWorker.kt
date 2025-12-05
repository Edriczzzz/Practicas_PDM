package com.example.practica3room.worker


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.practica3room.di.AppContainer

class TaskSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "TaskSyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🔄 Iniciando sincronización en background...")

            val repository = AppContainer.taskRepository
            val syncResult = repository.syncAll()

            if (syncResult.isSuccess) {
                Log.d(TAG, "✅ Sincronización exitosa")
                Result.success()
            } else {
                Log.e(TAG, "❌ Error en sincronización: ${syncResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción en worker", e)
            Result.retry()
        }
    }
}