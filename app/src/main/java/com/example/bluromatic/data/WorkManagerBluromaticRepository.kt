/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.getImageUri
import com.example.bluromatic.workers.BlurWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {
    
    val imageUri = context.getImageUri()

    override val outputWorkInfo: Flow<WorkInfo?> = MutableStateFlow(null)
    private val workManager = WorkManager.getInstance(context = context)

    /**
     * Create the WorkRequests to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    override fun applyBlur(blurLevel: Int) {
        
        /*
                This code produces and runs the
                following chain of WorkRequests: a CleanupWorker WorkRequest
                 followed by a BlurWorker WorkRequest
                 followed by a SaveImageToFileWorker WorkRequest.
        */
        
        
        //add workRequest to clean up temporary images
        // Blur-O-Matic now only blurs one image at a time.
        
        
        var continuation =
            workManager.beginUniqueWork(
                uniqueWorkName = IMAGE_MANIPULATION_WORK_NAME,
                existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                request = OneTimeWorkRequest.from(workerClass =  CleanUpWorker::class.java)
            )
        //add workRequst to blur the image
        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
        blurBuilder.setInputData(inputData = createInputDataForWorkRequest(blurLevel,imageUri))
        continuation = continuation.then(blurBuilder.build()) /* add the blurBuilder to the chain*/
        /* add work request to save the image to the filesystem */
        var save  = OneTimeWorkRequestBuilder<SaveImageToFileWorker>().build()
        continuation = continuation.then(save)
        continuation.enqueue() /* start the work */
    }

    /**
     * Cancel any ongoing WorkRequests
     **/
    override fun cancelWork() {}

    /**
     * Creates the input data bundle which includes the blur level to
     * update the amount of blur to be applied and the Uri to operate on
     * @return Data which contains the Image Uri as a String and blur level as an Integer
     */
    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString()).putInt(KEY_BLUR_LEVEL, blurLevel)
        return builder.build()
    }
}
