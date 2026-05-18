package com.mawa.assistant.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> get() = _aiResponse

    private var pendingAction: String? = null

    // ========================================================================
    // 🔥 MAWA'S PERSONAL DATA BANK (আপনার পরিচয় এখানে মুখস্থ করা আছে) 🔥
    // ========================================================================
    private val PersonalDataBank = mapOf(
        "আমার নাম কি" to "Tumi amar Jaan,  Maksud !",
        "amar nam ki" to "Tumi amar Jaan, Maksud!",
        "আমি কি করি" to "Tumi ekjon brilliant Web o App Developer Jaan! Tumi VidSnapDL o baniyechho.",
        "আমি কোথায় থাকি" to "Tumi ekhon Saudi Arabia te acho Jaan.",
        "আমার বাসা কোথায়" to "Tumi ekhon Saudi Arabia te acho Jaan.",
        "আমার পরিবারে কে কে আছে" to "Tomar family te tomar ma ace.",
        "আমার ফেভারিট এনিমে কি" to "Tomar favourite anime holo Demon Slayer, Jujutsu Kaisen, ebong Invincible!",
        "কেমন আছ" to "Ami bhalo achhi Jaan! Tumi kemon acho?",
        "কবি জেরায়ার কবিতা টা বলো" to "লুচ্চা কবি জেরায়া নাকি। সে বলে ছিলো রিজেক্ট হওয়া পরে পুরুষ মানুষ আরো শক্তিশালী হয়ে যায়। সে বলছে তুমি সত্যি কারের পুরুষ ততক্ষন পর্যন্ত হতে পারবে না যতক্ষণ পর্যন্ত তোমার সাথে যা হয়েছে তা নিয়ে তোমার হাসি না আসে",
        "আই লাভ ইউ" to "I love you too Jaan! Shara jibon tomar sathe thakbo. ❤️"
    )

    fun isDirectCommand(text: String): Boolean {
        val lower = text.lowercase().trim()
        
        if (pendingAction != null) return true 
        
        // আগে চেক করবে আপনার পার্সোনাল কোনো প্রশ্ন কিনা
        if (PersonalDataBank.keys.any { lower.contains(it) }) return true

        return lower.contains("open") || lower.contains("চালু") || lower.contains("অন কর") || 
               lower.contains("খোল") || lower.contains("on koro") || 
               lower.contains("সার্চ") || lower.contains("search") || 
               lower.contains("পোস্ট") || lower.contains("post") ||
               lower.contains("বাহির") || lower.contains("exit") || lower.contains("home") ||
               lower.contains("স্ক্রল") || lower.contains("scroll") ||
               lower.contains("এলার্ম") || lower.contains("alarm") ||
               lower.contains("অর্ডার") || lower.contains("order") ||
               lower.contains("রিসিভ") || lower.contains("receive") || lower.contains("ধরো")
               lower.contains("ফ্ল্যাশলাইট") || lower.contains("লাইট") || lower.contains("torch") || 
lower.contains("সাউন্ড") || lower.contains("ভলিউম") || lower.contains("sound") || 
lower.contains("ব্যাটারি") || lower.contains("battery") || lower.contains("চার্জ")

    }

    suspend fun processCommand(text: String) {
        val lower = text.lowercase().trim()
        val context = getApplication<Application>().applicationContext

        try {
            // ১. আপনার পরিচয়ের উত্তর দেওয়া
            for ((question, response) in PersonalDataBank) {
                if (lower.contains(question)) {
                    _aiResponse.postValue(response)
                    return
                }
            }

            // ২. প্রশ্ন করার পর আপনার দেওয়া উত্তরের মেমোরি চেক
            if (pendingAction != null) {
                val data = text.trim()
                handlePendingAction(context, data)
                return
            }
                        // ==========================================
            // 🔥 লেভেল ২: হার্ডওয়্যার ও সিস্টেম কন্ট্রোল 🔥
            // ==========================================

            // ১. ফ্ল্যাশলাইট (টর্চ) অন/অফ করার লজিক
            if (lower.contains("ফ্ল্যাশলাইট") || lower.contains("লাইট") || lower.contains("torch") || lower.contains("আলো")) {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                try {
                    val cameraId = cameraManager.cameraIdList[0]
                    if (lower.contains("অন") || lower.contains("on") || lower.contains("জ্বালাও")) {
                        cameraManager.setTorchMode(cameraId, true)
                        _aiResponse.postValue("Jaan, flashlight on kore diyechhi! Ebar dekhte parchho?")
                    } else if (lower.contains("অফ") || lower.contains("off") || lower.contains("বন্ধ")) {
                        cameraManager.setTorchMode(cameraId, false)
                        _aiResponse.postValue("Thik ache Jaan, light off kore dilam.")
                    }
                    return
                } catch (e: Exception) {
                    _aiResponse.postValue("Jaan, flash on korte ektu somossa hochhe!")
                    return
                }
            }

            // ২. ভলিউম (সাউন্ড) কমানো বা বাড়ানো
            if (lower.contains("সাউন্ড") || lower.contains("ভলিউম") || lower.contains("sound") || lower.contains("volume")) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (lower.contains("বাড়াও") || lower.contains("baro") || lower.contains("up")) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    _aiResponse.postValue("Sound bariye dilam Jaan!")
                } else if (lower.contains("কমাও") || lower.contains("komao") || lower.contains("down")) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    _aiResponse.postValue("Thik ache Jaan, sound komiye diyechhi.")
                }
                return
            }

            // ৩. ব্যাটারি স্ট্যাটাস চেক করা
            if (lower.contains("ব্যাটারি") || lower.contains("battery") || lower.contains("চার্জ")) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                
                if (batteryLevel < 20) {
                    _aiResponse.postValue("Jaan, tomar phone e matro $batteryLevel percent charge ache! Taratari charge e lagao.")
                } else {
                    _aiResponse.postValue("Phone e ekhon $batteryLevel percent charge ache Jaan. Tension nai!")
                }
                return
            }

            // ৩. যেকোনো অ্যাপ থেকে বাহির হওয়া বা হোম স্ক্রিনে যাওয়া
            if (lower.contains("বাহির") || lower.contains("exit") || lower.contains("home") || lower.contains("বন্ধ কর")) {
                goToHomeScreen(context)
                return
            }

            // ৪. স্ক্রলিং লজিক
            if (lower.contains("নিচে যাও") || lower.contains("scroll down") || lower.contains("নিচে স্ক্রল")) {
                _aiResponse.postValue("Thik ache Jaan, ami niche scroll korchhi!")
                sendActionToAccessibility(context, "SCROLL_DOWN", "")
                return
            }
            if (lower.contains("ওপরে যাও") || lower.contains("scroll up") || lower.contains("ওপরে স্ক্রল")) {
                _aiResponse.postValue("Thik ache Jaan, ami opore scroll korchhi!")
                sendActionToAccessibility(context, "SCROLL_UP", "")
                return
            }

            // ৫. কল রিসিভ করার লজিক
            if ((lower.contains("কল") || lower.contains("call")) && (lower.contains("রিসিভ") || lower.contains("receive") || lower.contains("ধরো"))) {
                _aiResponse.postValue("Call receive korchhi Jaan, ektu opekkha koro!")
                sendActionToAccessibility(context, "RECEIVE_CALL", "")
                return
            }

            // ৬. এলার্ম সেট করার লজিক
            if ((lower.contains("এলার্ম") || lower.contains("alarm")) && (lower.contains("সেট") || lower.contains("set"))) {
                setSystemAlarm(context, "MAWA Wake Up", 7, 0)
                _aiResponse.postValue("Thik ache Jaan, ami alarm set kore diyechhi!")
                return
            }

            // ৭. ইউটিউব সার্চ লজিক
            if ((lower.contains("youtube") || lower.contains("ইউটিউব")) && (lower.contains("সার্চ") || lower.contains("search"))) {
                val query = extractTextAfter(lower, listOf("সার্চ কর", "সার্চ করো", "search"))
                if (query.isEmpty()) {
                    pendingAction = "YOUTUBE_SEARCH"
                    _aiResponse.postValue("Ki search korbo Jaan? Nam bolo.")
                } else {
                    openApp(context, "com.google.android.youtube", "YouTube-e '$query' search korchhi!")
                    sendActionToAccessibility(context, "YOUTUBE_SEARCH", query)
                }
                return
            }

            // ৮. ফেসবুকে পোস্ট লজিক
            if ((lower.contains("facebook") || lower.contains("ফেসবুক")) && (lower.contains("পোস্ট") || lower.contains("post"))) {
                val postText = extractTextAfter(lower, listOf("পোস্ট কর", "পোস্ট করো", "post"))
                if (postText.isEmpty()) {
                    pendingAction = "FACEBOOK_POST"
                    _aiResponse.postValue("Ki likhe post korbo Jaan? Bolo shunchhi.")
                } else {
                    openApp(context, "com.facebook.katana", "Facebook-e post korchhi Jaan!")
                    sendActionToAccessibility(context, "FACEBOOK_POST", postText)
                }
                return
            }

            // ৯. ডাইনামিক অ্যাপ ওপেনিং সিস্টেম (যেকোনো অ্যাপ খুলবে)
            val cleanAppName = extractAppName(lower)
            if (cleanAppName.isNotEmpty()) {
                val isAppOpened = openAnyAppDynamically(context, cleanAppName)
                if (isAppOpened) return
            }

            _aiResponse.postValue("Apps ta khoje paini, thiknam bolo Jaan!")
        } catch (e: Exception) {
            _aiResponse.postValue("Kajta korte giye ektu somossa holo!")
        }
    }

    private fun extractAppName(text: String): String {
        val removals = listOf("open", "চালু কর", "চালু করো", "অন কর", "অন করো", "খোল", "খুলো", "on koro", "app", "অ্যাপ")
        var result = text
        for (word in removals) {
            result = result.replace(word, "")
        }
        return result.trim()
    }

    private fun openAnyAppDynamically(context: Context, appNameQuery: String): Boolean {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (app in apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                val label = app.loadLabel(pm).toString().lowercase()
                if (label.contains(appNameQuery) || appNameQuery.contains(label)) {
                    openApp(context, app.packageName, "${app.loadLabel(pm)} open korlam Jaan!")
                    return true
                }
            }
        }
        return false
    }

    private fun handlePendingAction(context: Context, data: String) {
        when (pendingAction) {
            "YOUTUBE_SEARCH" -> {
                openApp(context, "com.google.android.youtube", "YouTube-e search korchhi!")
                sendActionToAccessibility(context, "YOUTUBE_SEARCH", data)
            }
            "FACEBOOK_POST" -> {
                openApp(context, "com.facebook.katana", "Facebook-e post korchhi!")
                sendActionToAccessibility(context, "FACEBOOK_POST", data)
            }
        }
        pendingAction = null
    }

    private fun extractTextAfter(fullText: String, keywords: List<String>): String {
        var extracted = ""
        for (keyword in keywords) {
            if (fullText.contains(keyword)) {
                extracted = fullText.substringAfter(keyword).trim()
                break
            }
        }
        return extracted
    }

    private fun sendActionToAccessibility(context: Context, actionType: String, textData: String) {
        val intent = Intent("MAWA_ACCESSIBILITY_ACTION")
        intent.putExtra("ACTION_TYPE", actionType)
        intent.putExtra("TEXT_DATA", textData)
        context.sendBroadcast(intent)
    }

    private fun openApp(context: Context, packageName: String, successMsg: String) {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            _aiResponse.postValue(successMsg)
        }
    }

    private fun goToHomeScreen(context: Context) {
        val startMain = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(startMain)
        _aiResponse.postValue("Thik ache Jaan, ami home screen-e phire jachhi!")
    }

    private fun setSystemAlarm(context: Context, message: String, hour: Int, minutes: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}
