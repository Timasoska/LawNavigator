package com.example.lawnavigator.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
    private val repository: ContentRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        // Если система просит создать FlashcardWorker, мы делаем это вручную, передавая репозиторий
        return when (workerClassName) {
            FlashcardWorker::class.java.name ->
                FlashcardWorker(appContext, workerParameters, repository)
            else -> null
        }
    }
}