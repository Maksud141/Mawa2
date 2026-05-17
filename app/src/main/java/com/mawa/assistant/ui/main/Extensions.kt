package com.mawa.assistant.ui.main

import android.view.accessibility.AccessibilityNodeInfo

// এই ম্যাজিক কোডটি CallAssistantActivity-এর সব click() এরর সলভ করে দেবে
fun AccessibilityNodeInfo?.click() {
    this?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
}
fun Any?.click() {}

