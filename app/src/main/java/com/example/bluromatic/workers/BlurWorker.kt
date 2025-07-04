package com.example.bluromatic.workers

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "blurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) :
        CoroutineWorker(appContext = ctx, params = params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)
        makeStatusNotification(
            message = applicationContext.resources.getString(R.string.blurring_image),
            context = applicationContext
        )
        
        return withContext(context = Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS)
            return@withContext try {
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                }
                val resolver = applicationContext.contentResolver
                
                val picture =
                    BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
                
                val output = blurBitmap(bitmap = picture, blurLevel)
                val outputUri = writeBitmapToFile(
                    applicationContext = applicationContext, bitmap = output
                )
                
                val outputData =workDataOf(KEY_IMAGE_URI to outputUri.toString())
                
                    // makeStatusNotification(message = "Output $outputUri", context = applicationContext)
                
                Result.success(outputData)
            } catch (throwable: Throwable) {
                Log.e(
                    "blurImage",
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )
                Result.failure()
            }
        }
    }
}