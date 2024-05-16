package tech.hotash.hotms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat

class SmsService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        val channel = "SMS_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                NotificationChannel(channel, "Foreground Notification", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        return NotificationCompat.Builder(this, channel)
            .setContentTitle("SMS Service")
            .setContentText("Listening for incoming SMS")
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Starting Service", Toast.LENGTH_SHORT).show()
        return super.onStartCommand(intent, flags, startId)
    }
}
