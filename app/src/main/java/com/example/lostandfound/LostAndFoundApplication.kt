package com.example.lostandfound

import android.app.Application
import com.example.lostandfound.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class
 * Initialize Koin dependency injection
 */
class LostAndFoundApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            // Log Koin into Android logger
            androidLogger(Level.ERROR)

            // Reference Android context
            androidContext(this@LostAndFoundApplication)

            // Load modules
            modules(appModules)
        }
    }
}