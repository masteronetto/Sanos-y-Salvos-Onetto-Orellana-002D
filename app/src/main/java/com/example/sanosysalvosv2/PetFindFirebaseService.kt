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
import android.content.Intent
import android.util.Log
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
                Log.e("FCM", "Failed to update device token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "PetFind"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "system"
        showNotification(title, body, type)
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = "petfind_notifications"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channel = NotificationChannel(
            channelId,
            "Notificaciones PetFind",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Coincidencias y reportes de mascotas"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
