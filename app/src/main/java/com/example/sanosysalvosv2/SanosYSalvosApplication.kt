package com.example.sanosysalvosv2

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SanosYSalvosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val matchesChannel = NotificationChannel(
                "matches_channel", "Coincidencias",
                NotificationManager.IMPORTANCE_HIGH
            )

            val reportsChannel = NotificationChannel(
                "reports_channel", "Reportes",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(matchesChannel)
            manager.createNotificationChannel(reportsChannel)
        }
    }
}
