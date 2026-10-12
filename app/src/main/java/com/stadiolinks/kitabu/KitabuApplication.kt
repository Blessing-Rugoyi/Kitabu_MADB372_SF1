package com.stadiolinks.kitabu

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import com.stadiolinks.kitabu.di.AppContainer
import com.stadiolinks.kitabu.di.DefaultAppContainer

class KitabuApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        enableStrictModeLogging()
        container = DefaultAppContainer(this)
    }

    /**
     * Debug builds only: writes a "StrictMode" warning to Logcat whenever disk, network or
     * slow work happens on the main thread, which is the usual cause of "App not responding".
     */
    private fun enableStrictModeLogging() {
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (!isDebuggable) return
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }
}
