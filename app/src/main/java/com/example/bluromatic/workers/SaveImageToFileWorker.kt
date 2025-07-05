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
        makeStatusNotification(
            message = applicationContext.resources.getString(R.string.saving_image),
            context = applicationContext
        )
        
        return withContext(context = Dispatchers.IO) {
            delay(timeMillis = DELAY_TIME_MILLIS)
            val resolver = applicationContext.contentResolver
            return@withContext try {
                val resourceUri =
                    inputData.getString(KEY_IMAGE_URI) /* input data ---- temporarily blurred image URI */
                val bitmap =
                    BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
                val imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, title, dateFormater.format(
                        Date()
                    )
                )
                /* if (!imageUrl.isNotEmpty()) */
                if (imageUrl.isEmpty()){
                    val output = workDataOf(KEY_IMAGE_URI to imageUrl)
                    Result.success(output)
                }else{
                    Log.e(TAG,applicationContext.resources.getString(R.string.writing_to_mediaStore_failed))
                    Result.failure()
                }
                
                Result.success()
            } catch (exception: Exception) {
                Log.e(TAG,applicationContext.resources.getString(R.string.error_saving_image),exception)
                Result.failure()
                
            }
        }
        
        
    }
    
    
}