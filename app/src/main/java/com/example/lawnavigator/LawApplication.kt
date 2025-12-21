package com.example.lawnavigator
import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.example.lawnavigator.worker.CustomWorkerFactory


@HiltAndroidApp
class LawApplication : Application() {

    @Inject
    lateinit var customWorkerFactory: CustomWorkerFactory // Инжектим нашу фабрику

    override fun onCreate() {
        super.onCreate()

        // Инициализируем WorkManager с нашей фабрикой
        val config = Configuration.Builder()
            .setWorkerFactory(customWorkerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

        try {
            WorkManager.initialize(this, config)
        } catch (e: Exception) {
            Log.w("LawApplication", "WorkManager init failed: ${e.message}")
        }
    }
}