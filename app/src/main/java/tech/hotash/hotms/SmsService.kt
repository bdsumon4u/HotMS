package tech.hotash.hotms

import android.app.Notification
import android.app.Service
import android.content.Intent
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
        return NotificationCompat.Builder(this, SmsApp.CHANNEL)
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
