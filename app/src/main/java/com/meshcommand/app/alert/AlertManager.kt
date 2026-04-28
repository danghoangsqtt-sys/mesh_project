package com.meshcommand.app.alert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.meshcommand.app.data.entity.EventEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced Alert System — handles push notifications, vibration patterns,
 * and alert sounds for SOS, Man Down, and critical battery events.
 */
@Singleton
class AlertManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ALERTS = "mesh_alerts"
        private const val CHANNEL_NAME = "Mesh Alerts"
        private var notificationId = 2000
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ALERTS,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tactical alerts for SOS, Man Down, critical events"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun processAlert(event: EventEntity) {
        when (event.severity) {
            2 -> handleCriticalAlert(event)
            1 -> handleWarningAlert(event)
        }
    }

    private fun handleCriticalAlert(event: EventEntity) {
        // Push notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 CRITICAL ALERT")
            .setContentText(event.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId++, notification)

        // Vibration — SOS pattern (... --- ...)
        vibrate(longArrayOf(0, 200, 100, 200, 100, 200, 300, 500, 300, 500, 300, 500, 300, 200, 100, 200, 100, 200))
    }

    private fun handleWarningAlert(event: EventEntity) {
        // Push notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚠️ WARNING")
            .setContentText(event.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId++, notification)

        // Short double vibration
        vibrate(longArrayOf(0, 200, 100, 200))
    }

    private fun vibrate(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}
