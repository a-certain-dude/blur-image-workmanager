package com.example.bluromatic.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Locale

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(appContext = ctx, params = params) {
    private val title = "blurred image"
    private val dateFormater = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss Z ", Locale.getDefault())
    override suspend fun doWork(): Result {
        return
    }
    
    
}