package com.mawa.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> get() = _aiResponse

    fun isDirectCommand(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("call ") || lower.contains("open ") || lower.contains("play ")
    }

    suspend fun processCommand(text: String) {
        // Basic fallback execution
        _aiResponse.postValue("Command received")
    }
}
