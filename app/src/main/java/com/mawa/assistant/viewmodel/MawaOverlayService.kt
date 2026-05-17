package com.mawa.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MawaOverlayService : Service() {
    companion object {
        fun updateState(context: Context, speaking: Boolean) {
            // UI state update
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "mawa_service_channel"
        
        // অ্যান্ড্রয়েড ৮ (Oreo) বা তার ওপরের ভার্সনের জন্য নোটিফিকেশন চ্যানেল বানানো বাধ্যতামূলক
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MAWA Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // নোটিফিকেশন তৈরি করা
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MAWA Assistant")
            .setContentText("MAWA is running in background ❤️")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now) // ডিফল্ট মাইক আইকন
            .build()

        // সার্ভিসকে ব্যাকগ্রাউন্ডে চালু রাখা
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
