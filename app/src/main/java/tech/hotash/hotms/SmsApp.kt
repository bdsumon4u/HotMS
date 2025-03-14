package tech.hotash.hotms

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SmsApp: Application() {
    companion object{
        const val CHANNEL = "SMS_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                NotificationChannel(CHANNEL, "Foreground Notification", NotificationManager.IMPORTANCE_HIGH)
                    .apply {
                        description = "Channel for displaying foreground notifications"
                    }
            )
        }
    }
}