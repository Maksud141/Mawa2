package com.mawa.assistant.utils

import android.view.accessibility.AccessibilityNodeInfo

object ScreenReader {
    fun dump(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val sb = java.lang.StringBuilder()
        extractText(node, sb)
        return sb.toString()
    }

    private fun extractText(node: AccessibilityNodeInfo?, sb: java.lang.StringBuilder) {
        if (node == null) return
        if (node.text != null) sb.append(node.text).append(" ")
        else if (node.contentDescription != null) sb.append(node.contentDescription).append(" ")
        
        for (i in 0 until node.childCount) {
            extractText(node.getChild(i), sb)
        }
    }
}
