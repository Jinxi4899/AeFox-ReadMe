package aefox.reyang.ui.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import aefox.reyang.R

// System notification helper for the shell. Both the status-bar small icon and
// the large avatar are always attached so later features only need one call.
object AeFoxNotifier {
    const val CHANNEL_ID: String = "aefox_general"
    private const val CHANNEL_NAME: String = "AeFox notifications"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "AeFox general notifications"
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun show(context: Context, id: Int, title: String, text: String) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.aefox_icon_round,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_aefox)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(ContextCompat.getColor(context, R.color.aefox_brand))
            .setAutoCancel(true)
            .build()

        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
