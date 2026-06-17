// PART A — Manual setup before running this code:
// 1. Go to https://console.firebase.google.com
// 2. Create project "PetFind"
// 3. Add Android app with package: com.example.sanosysalvosv2
// 4. Download google-services.json → place in app/ folder
// 5. Done — the code below handles the rest

package com.example.sanosysalvosv2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.sanosysalvosv2.data.api.ProfileApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.UpdateProfileRequest
import com.example.sanosysalvosv2.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.core.app.NotificationCompat

class PetFindFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionStore = SessionStore(applicationContext)
                val authToken = sessionStore.tokenFlow.first() ?: return@launch
                val userId = sessionStore.userIdFlow.first() ?: return@launch
                val api = XanoRetrofitClient.retrofit.create(ProfileApi::class.java)
                api.updateProfile(
                    authHeader = "Bearer $authToken",
                    id = userId,
                    body = UpdateProfileRequest(deviceToken = token)
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to update device token")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("FCM Message received from: ${remoteMessage.from}")
        Timber.d("Data: ${remoteMessage.data}")

        val data = remoteMessage.data
        val matchId = data["match_id"] ?: data["matchId"]
        val reportId = data["report_id"] ?: data["reportId"]
        val score = data["score"]?.toDoubleOrNull() ?: 0.0

        when {
            !matchId.isNullOrEmpty() -> {
                Timber.d("Match notification: $matchId with score $score")
                showMatchNotification(matchId, score)
            }
            !reportId.isNullOrEmpty() -> {
                Timber.d("Report notification: $reportId")
                showReportNotification(reportId)
            }
            else -> {
                Timber.w("Unknown notification type: $data")
            }
        }
    }

    private fun showMatchNotification(matchId: String, score: Double) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notification_type", "match")
            putExtra("match_id", matchId)
            putExtra("matchId", matchId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            matchId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "matches_channel")
            .setContentTitle("¡Nueva coincidencia!")
            .setContentText("Puntuación: ${score.toInt()} %")
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannelIfNeeded(this, true)
        notificationManager.notify(matchId.hashCode(), notification)
    }

    private fun showReportNotification(reportId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notification_type", "report")
            putExtra("report_id", reportId)
            putExtra("reportId", reportId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            reportId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "reports_channel")
            .setContentTitle("Nuevo reporte")
            .setContentText("Toca para ver detalles")
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannelIfNeeded(this, false)
        notificationManager.notify(reportId.hashCode(), notification)
    }

    private fun NotificationManager.createNotificationChannelIfNeeded(context: Context, isMatchChannel: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = if (isMatchChannel) "matches_channel" else "reports_channel"
            val existingChannel = getNotificationChannel(channelId)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    channelId,
                    if (isMatchChannel) "Coincidencias" else "Reportes",
                    if (isMatchChannel) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = if (isMatchChannel) "Notificaciones cuando se detectan coincidencias" else "Notificaciones de nuevos reportes"
                    enableVibration(isMatchChannel)
                    if (isMatchChannel) enableLights(true)
                }
                createNotificationChannel(channel)
            }
        }
    }
}
