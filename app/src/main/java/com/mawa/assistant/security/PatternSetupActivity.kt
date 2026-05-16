package com.mawa.assistant.security

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mawa.assistant.R
import java.util.*

class PatternSetupActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var patternLockView: PatternLockView
    private lateinit var instructionText: TextView
    private lateinit var retryBtn: Button
    private lateinit var cancelBtn: Button
    
    private var tts: TextToSpeech? = null
    private var firstPattern: List<Int>? = null
    private var isConfirming = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_setup)

        initViews()
        tts = TextToSpeech(this, this)
    }

    private fun initViews() {
        patternLockView = findViewById(R.id.patternLockView)
        instructionText = findViewById(R.id.instructionText)
        retryBtn = findViewById(R.id.retryBtn)
        cancelBtn = findViewById(R.id.cancelBtn)

        patternLockView.listener = object : PatternLockView.PatternListener {
            override fun onPatternStarted() {
                instructionText.text = if (isConfirming) "Abar draw koro..." else "Connect korte thako..."
            }

            override fun onPatternComplete(pattern: List<Int>) {
                handlePattern(pattern)
            }

            override fun onPatternCleared() {}
        }

        retryBtn.setOnClickListener { resetSetup() }
        cancelBtn.setOnClickListener { finish() }
    }

    private fun handlePattern(pattern: List<Int>) {
        if (!isConfirming) {
            // First time drawing
            if (pattern.size < PatternLockView.MIN_PATTERN_LENGTH) {
                patternLockView.showError()
                instructionText.text = "Onek choto! Kom kore 4 ta dot connect koro"
                speak("Kom kore 4 ta dot connect koro", true)
                handler.postDelayed({ patternLockView.clearPattern() }, 800)
                return
            }

            firstPattern = pattern
            patternLockView.showSuccess()
            speak("Ebar abar draw kore confirm koro", true)
            
            handler.postDelayed({
                isConfirming = true
                instructionText.text = "Confirm korar jonno abar draw koro"
                patternLockView.clearPattern()
                retryBtn.visibility = View.VISIBLE
            }, 700)
            
        } else {
            // Confirming the pattern
            val first = firstPattern ?: return
            if (pattern.joinToString("-") == first.joinToString("-")) {
                // Success!
                PatternManager.savePattern(this, pattern)
                PatternManager.enablePatternLock(this)
                SecurityManager.setAppLockEnabled(this, true)
                patternLockView.showSuccess()
                instructionText.text = "Pattern set hoye geche!"
                speak("Pattern set hoye geche! Ebar apnar app safe.", true)
                
                handler.postDelayed({
                    setResult(RESULT_OK)
                    finish()
                }, 1500)
            } else {
                // Mismatch
                patternLockView.showError()
                instructionText.text = "Pattern match hoyni! Try again"
                speak("Pattern match hoyni! Abar try koro", true)
                handler.postDelayed({ patternLockView.clearPattern() }, 800)
            }
        }
    }

    private fun resetSetup() {
        firstPattern = null
        isConfirming = false
        patternLockView.clearPattern()
        retryBtn.visibility = View.INVISIBLE
        instructionText.text = "Naya pattern draw koro"
        speak("Thik ache, naya pattern draw koro", true)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("bn", "BD")
        }
    }

    private fun speak(text: String, flush: Boolean) {
        val mode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, mode, null, null)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
