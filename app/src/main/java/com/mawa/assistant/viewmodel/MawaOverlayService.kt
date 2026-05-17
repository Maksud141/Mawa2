package com.mawa.assistant.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class MawaOverlayService : Service() {
    companion object {
        fun updateState(context: Context, speaking: Boolean) {
            // UI state update
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
