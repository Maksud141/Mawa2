package com.mawa.assistant.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AssistLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // এর কাজ হলো হোম বাটন চাপলে সরাসরি মায়ার মেইন অ্যাপটা ওপেন করে দেওয়া
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        
        // কাজ শেষ করে নিজে বন্ধ হয়ে যাবে
        finish()
    }
}
