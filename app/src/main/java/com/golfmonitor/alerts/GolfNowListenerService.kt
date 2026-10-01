package com.golfmonitor.alerts

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Reads GolfNow notifications once Paul grants notification access. Everything else
 * on the phone is ignored; captured text stays on the device.
 */
class GolfNowListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!AlertIngestor.isGolfNowPackage(sbn.packageName)) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString("\n")
        val body = listOfNotNull(text, lines).joinToString("\n")
        if (body.isBlank() && title.isNullOrBlank()) return

        val receivedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(sbn.postTime), ZoneId.systemDefault())
        scope.launch {
            AlertIngestor.ingest(applicationContext, sbn.packageName, title, body, receivedAt)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun isEnabled(context: Context): Boolean =
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }
}
