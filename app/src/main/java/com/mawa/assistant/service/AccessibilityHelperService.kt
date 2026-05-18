package com.mawa.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityHelperService : AccessibilityService() {

    // একদম ফাইনাল ম্যাজিক! ব্র্যাকেটের ভেতর Context রিসিভ করার জায়গা দেওয়া হলো
    companion object {
        var instance: AccessibilityHelperService? = null
        
        fun isEnabled(context: Context): Boolean {
            return instance != null
        }
    }

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val actionType = intent?.getStringExtra("ACTION_TYPE")
            val textData = intent?.getStringExtra("TEXT_DATA") ?: ""

            when (actionType) {
                "SCROLL_DOWN" -> scrollScreen(isDown = true)
                "SCROLL_UP" -> scrollScreen(isDown = false)
                "RECEIVE_CALL" -> autoReceiveCall()
                "YOUTUBE_SEARCH" -> autoYouTubeSearch(textData)
                "FACEBOOK_POST" -> autoFacebookPost(textData)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val filter = IntentFilter("MAWA_ACCESSIBILITY_ACTION")
        registerReceiver(actionReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        try {
            unregisterReceiver(actionReceiver)
        } catch (e: Exception) {}
    }
    
    private fun autoReceiveCall() {
        val rootNode = rootInActiveWindow ?: return
        val keywords = listOf("রিসিভ", "Accept", "Answer", "ধরো", "কল ধরো")
        
        for (keyword in keywords) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
            if (nodes.isNotEmpty()) {
                nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
    } // <-- ভাই, আপনার এই ব্র্যাকেটটা মিসিং ছিল!

    // 🔥 লেভেল ১: মেসেজ পড়ে শোনানোর ম্যাজিক লজিক 🔥
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        if (event?.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED || 
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            // হোয়াটসঅ্যাপ (WhatsApp) মেসেজ স্ক্যান করা
            if (event.packageName == "com.whatsapp") {
                val messageList = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/message_text")
                if (messageList.isNotEmpty()) {
                    val latestMessage = messageList.last().text?.toString() ?: ""
                    
                    if (latestMessage.isNotEmpty()) {
                        sendActionToViewModel("READ_MESSAGE", latestMessage)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        // সার্ভিস ইন্টারাপ্ট হলে আপাতত কিছু করার দরকার নেই
    }

    private fun scrollScreen(isDown: Boolean) {
        val path = android.graphics.Path()
        if (isDown) {
            path.moveTo(500f, 1500f)
            path.lineTo(500f, 500f)
        } else {
            path.moveTo(500f, 500f)
            path.lineTo(500f, 1500f)
        }

        val gestureBuilder = android.accessibilityservice.GestureDescription.Builder()
        gestureBuilder.addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 400))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    // মেইন ব্রেইনে (ViewModel) ডাটা পাঠানোর হেল্পার ফাংশন
    private fun sendActionToViewModel(action: String, data: String) {
        val intent = android.content.Intent("MAWA_ACCESSIBILITY_ACTION").apply {
            putExtra("ACTION_TYPE", action)
            putExtra("TEXT_DATA", data)
        }
        sendBroadcast(intent)
    }

    // <-- ভাই, এখানে আপনার একটা বাড়তি ব্র্যাকেট ছিল, সেটা আমি রিমুভ করে দিয়েছি!

    private fun autoYouTubeSearch(query: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            val searchButtons = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/search_button")
            
            if (searchButtons.isNotEmpty()) {
                searchButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                
                Handler(Looper.getMainLooper()).postDelayed({
                    val currentRoot = rootInActiveWindow ?: return@postDelayed
                    val editTexts = currentRoot.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/search_edit_text")
                    if (editTexts.isNotEmpty()) {
                        val arguments = Bundle()
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, query)
                        editTexts[0].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                    }
                }, 1000)
            }
        }, 1500)
    }

    private fun autoFacebookPost(text: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            var textNodes = rootNode.findAccessibilityNodeInfosByText("What's on your mind?")
            if (textNodes.isEmpty()) textNodes = rootNode.findAccessibilityNodeInfosByText("এখানে কিছু লিখুন")
            
            if (textNodes.isNotEmpty()) {
                textNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                
                Handler(Looper.getMainLooper()).postDelayed({
                    val currentRoot = rootInActiveWindow ?: return@postDelayed
                    val focusedNode = currentRoot.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                    
                    if (focusedNode != null) {
                        val arguments = Bundle()
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                        focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                        
                        Handler(Looper.getMainLooper()).postDelayed({
                            val postRoot = rootInActiveWindow ?: return@postDelayed
                            var postButtons = postRoot.findAccessibilityNodeInfosByText("POST")
                            if (postButtons.isEmpty()) postButtons = postRoot.findAccessibilityNodeInfosByText("পোস্ট করুন")
                            
                            if (postButtons.isNotEmpty()) {
                                postButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }, 1500)
                    }
                }, 1500)
            }
        }, 2000)
    }
}
