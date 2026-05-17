package com.mawa.assistant.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent

class AccessibilityHelperService : AccessibilityService() {
    companion object {
        @JvmStatic
        var instance: AccessibilityHelperService? = null
        
        @JvmStatic
        fun isEnabled(context: Context): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
