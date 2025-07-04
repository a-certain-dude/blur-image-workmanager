package com.example.bluromatic.workers

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.R

class CleanUpWorker(ctx: Context, params: WorkerParameters) :
        CoroutineWorker(appContext = ctx, params = params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        makeStatusNotification(
            message = applicationContext.resources.getString(R.string.cleaning_up_files),
            context = applicationContext
        )
    }
}