package com.example.bluromatic.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class BlurWorker(ctx: Context, params: WorkerParameters) :
        CoroutineWorker(appContext = ctx, params = params) {
    override suspend fun doWork(): Result {
        makeStatusNotification(
            message = applicationContext.getString(R.string.blurring_image),
            context = applicationContext
        )
        
        return  withContext(context = Dispatchers.IO){
            return@withContext try {
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                }
                val resolver = applicationContext.contentResolver
                
                val picture =
                    BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
                
                val output = blurBitmap(bitmap = picture, blurLevel = 1)
                val outputUri = writeBitmapToFile(
                    applicationContext = applicationContext, bitmap = output
                )
                
                val outputData =workDataOf(KEY_IMAGE_URI to outputUri.toString())
                
                makeStatusNotification(message = "Output $outputUri", context = applicationContext)
                
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