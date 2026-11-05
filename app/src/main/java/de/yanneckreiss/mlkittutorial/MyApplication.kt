package de.yanneckreiss.mlkittutorial

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.yanneckreiss.mlkittutorial.worker.UtilityAnomalyWorker

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule background WorkManager anomaly detection & alerts
        UtilityAnomalyWorker.schedulePeriodicCheck(this)
    }
}
