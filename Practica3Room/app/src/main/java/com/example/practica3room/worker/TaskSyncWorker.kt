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
        const val KEY_USER_ID = "user_id"
    }

    override suspend fun doWork(): Result {
        // 1) Prioridad: inputData
        val inputUserId = inputData.getInt(KEY_USER_ID, -1)

        // 2) Fallback: AppContainer
        val userId = if (inputUserId != -1) inputUserId else AppContainer.currentUserId

        if (userId == -1) return Result.retry()

        val result = AppContainer.taskRepository.syncAll(userId)
        return if (result.isSuccess) Result.success() else Result.retry()
    }



}