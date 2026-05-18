package com.mawa.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView

class AccessibilityHelperService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingBubble: ImageView? = null

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
        
        // 🔥 লেভেল ৪: ফ্লোটিং বাবল স্ক্রিনে নিয়ে আসার ম্যাজিক 🔥
        createFloatingBubble()
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
        
        // অ্যাপ বা সার্ভিস বন্ধ হলে বাবলটা সরিয়ে ফেলা
        floatingBubble?.let { windowManager?.removeView(it) }
    }
    
    // ==========================================
    // 🎈 ফ্লোটিং বাবল তৈরির ফাংশন 🎈
    // ==========================================
    private fun createFloatingBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        floatingBubble = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now) // মাইক আইকন
            setBackgroundColor(Color.parseColor("#FF1493")) // মায়ার জন্য কিউট গোলাপি রঙ
            setPadding(30, 30, 30, 30)
            
            // বাবলে ক্লিক করলে মায়া ওপেন হবে
            setOnClickListener {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }

        // বাবলটাকে সবার উপরে ভাসিয়ে রাখার সেটিং (কোনো এক্সট্রা পারমিশন লাগবে না)
        val params = WindowManager.LayoutParams(
            150, 
            150, 
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, 
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START 
        params.x = 20
        params.y = 300 // স্ক্রিনের একটু ওপরের দিকে থাকবে

        windowManager?.addView(floatingBubble, params)
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
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        // হোয়াটসঅ্যাপ (WhatsApp) মেসেজ স্ক্যান করা
        if (event?.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED || 
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
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

    override fun onInterrupt() {}

    private fun scrollScreen(isDown: Boolean) {
        val path = Path()
        if (isDown) {
            path.moveTo(500f, 1500f)
            path.lineTo(500f, 500f)
        } else {
            path.moveTo(500f, 500f)
            path.lineTo(500f, 1500f)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 400))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun sendActionToViewModel(action: String, data: String) {
        val intent = Intent("MAWA_ACCESSIBILITY_ACTION").apply {
            putExtra("ACTION_TYPE", action)
            putExtra("TEXT_DATA", data)
        }
        sendBroadcast(intent)
    }

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
