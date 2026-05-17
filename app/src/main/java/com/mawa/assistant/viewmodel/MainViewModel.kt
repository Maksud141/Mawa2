package com.mawa.assistant.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> get() = _aiResponse

    // বাংলা কথা শুনেই সরাসরি কাজ করার লিস্ট
    fun isDirectCommand(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("open") || lower.contains("চালু") || lower.contains("অন কর") || lower.contains("খোল") || lower.contains("on kor")
    }

    suspend fun processCommand(text: String) {
        val lower = text.lowercase()
        val context = getApplication<Application>().applicationContext

        try {
            if (text.startsWith("OPEN_APP ")) {
                val appName = text.removePrefix("OPEN_APP ").trim().lowercase()
                openAppByName(context, appName)
                return
            }
            
            // "পেইজ বুক" বা "ফেইসবুক" যেটাই শুনুক, কাজ করবে!
            if (lower.contains("facebook") || lower.contains("ফেসবুক") || lower.contains("ফেইসবুক") || lower.contains("পেইজ বুক")) {
                openApp(context, "com.facebook.katana", "Facebook open korlam Jaan!")
            } else if (lower.contains("youtube") || lower.contains("ইউটিউব") || lower.contains("utube")) {
                openApp(context, "com.google.android.youtube", "YouTube open korchhi!")
            } else if (lower.contains("whatsapp") || lower.contains("হোয়াটসঅ্যাপ") || lower.contains("whatsup")) {
                openApp(context, "com.whatsapp", "WhatsApp khulchhi!")
            } else if (lower.contains("tiktok") || lower.contains("টিকটক")) {
                openApp(context, "com.zhiliaoapp.musically", "TikTok open korchhi!")
            } else {
                _aiResponse.postValue("Gemini API connect koro, taholei shob parbo!")
            }
        } catch (e: Exception) {
            _aiResponse.postValue("Kajta korte giye ektu somossa holo!")
        }
    }

    private fun openAppByName(context: Context, appName: String) {
        when {
            appName.contains("facebook") || appName.contains("ফেসবুক") || appName.contains("পেইজ বুক") -> openApp(context, "com.facebook.katana", "Facebook open korchhi!")
            appName.contains("youtube") || appName.contains("ইউটিউব") -> openApp(context, "com.google.android.youtube", "YouTube open korchhi!")
            appName.contains("whatsapp") -> openApp(context, "com.whatsapp", "WhatsApp open korchhi!")
            else -> _aiResponse.postValue("Ei app ta amar chena nai!")
        }
    }

    private fun openApp(context: Context, packageName: String, successMsg: String) {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            _aiResponse.postValue(successMsg)
        } else {
            _aiResponse.postValue("App ta phone e install kora nai Jaan!")
        }
    }
}
