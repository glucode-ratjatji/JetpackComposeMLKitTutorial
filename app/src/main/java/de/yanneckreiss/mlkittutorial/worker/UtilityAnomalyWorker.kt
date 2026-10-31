package de.yanneckreiss.mlkittutorial.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.yanneckreiss.mlkittutorial.MainActivity
import de.yanneckreiss.mlkittutorial.core.database.AppDatabase
import de.yanneckreiss.mlkittutorial.entry.domain.model.AnomalySeverity
import de.yanneckreiss.mlkittutorial.entry.domain.util.AnomalyDetector
import java.util.concurrent.TimeUnit

class UtilityAnomalyWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "task_database"
            ).fallbackToDestructiveMigration().build()

            val recentEntries = db.entryDao().getRecentEntries(50)
            val anomalies = AnomalyDetector.detectAnomalies(recentEntries)

            val criticalOrWarning = anomalies.filter { it.severity != AnomalySeverity.INFO }
            if (criticalOrWarning.isNotEmpty()) {
                val latest = criticalOrWarning.first()
                showAnomalyNotification(latest.title, latest.message)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showAnomalyNotification(title: String, message: String) {
        val channelId = "utility_anomalies_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Utility Anomaly Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies users of unusual utility consumption spikes and potential leaks"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    companion object {
        private const val PERIODIC_WORK_TAG = "UtilityAnomalyPeriodicWorker"

        fun schedulePeriodicCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<UtilityAnomalyWorker>(
                12, TimeUnit.HOURS,
                1, TimeUnit.HOURS
            )
            .setConstraints(constraints)
            .addTag(PERIODIC_WORK_TAG)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun triggerImmediateCheck(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<UtilityAnomalyWorker>()
                .build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}
