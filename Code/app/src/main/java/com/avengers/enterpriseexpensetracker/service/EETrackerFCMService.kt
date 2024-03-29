package com.avengers.enterpriseexpensetracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.ui.activity.DashboardActivity
import com.avengers.enterpriseexpensetracker.util.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EETrackerFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("EETracker *******", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d("EETracker *******", "Message data payload: " + remoteMessage.data)
            Log.d("EETracker *******",
                    "Message data payload reportId: " + remoteMessage.data[Constants.FCM_PAYLOAD_REPORT_ID])
            Log.d("EETracker *******",
                    "Message data payload reportStatus: " + remoteMessage.data[Constants.FCM_PAYLOAD_REPORT_STATUS])
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("EETracker *******", "Message Notification Title: ${it.title}")
            Log.d("EETracker *******", "Message Notification Body: ${it.body}")
            it.body?.let { message -> sendNotification(message) }
        }
    }

    private fun sendNotification(messageBody: String) {

        val intent = Intent(this, DashboardActivity::class.java).apply {
            putExtra(Constants.EXTRA_SHOULD_START_NOTIFICATION, true)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.status_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle(getString(R.string.txt_status_update))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    getString(R.string.status_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
