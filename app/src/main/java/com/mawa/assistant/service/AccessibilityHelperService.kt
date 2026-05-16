package com.mawa.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AccessibilityHelperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // App open/close and other UI interactions will be handled here
        if (event == null) return
    }

    override fun onInterrupt() {
        Log.d("MAWA_Accessibility", "Service Interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.DEFAULT
        serviceInfo = info
        Log.d("MAWA_Accessibility", "Service Connected")
    }

    companion object {
        // This method is called from SettingsActivity to check status
        fun isEnabled(context: Context): Boolean {
            var accessibilityEnabled = 0
            val service = context.packageName + "/" + AccessibilityHelperService::class.java.canonicalName
            try {
                accessibilityEnabled = Settings.Secure.getInt(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
            }

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            if (accessibilityEnabled == 1) {
                val settingValue = Settings.Secure.getString(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    colonSplitter.setString(settingValue)
                    while (colonSplitter.hasNext()) {
                        val accessibilityService = colonSplitter.next()
                        if (accessibilityService.equals(service, ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }
}
