package com.mawa.assistant.ui.main

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mawa.assistant.R
import kotlin.math.abs
import kotlin.math.sin

// ─── Super Advanced WaveformView (JARVIS Level) ──────────────────────────────
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var amplitude = 0f
    private var phase = 0f
    private var isAnimating = true // সবসময় ব্রিদিং অ্যানিমেশন চলবে

    // স্মুথ ফিজিক্স এবং রিয়েল-টাইম রেন্ডারিং
    private val waveAnimator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
        duration = 1000 // একটু রিল্যাক্সিং স্পিড
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND // দাগের মাথাগুলো গোলাকার হবে
    }

    private val barCount = 25 // দাগের সংখ্যা বাড়ানো হয়েছে
    private val barHeights = FloatArray(barCount) { 0.05f }
    private var targetHeights = FloatArray(barCount) { 0.05f }

    init {
        // অ্যাপ ওপেন হলেই জ্যান্ত হয়ে যাবে
        waveAnimator.start()
    }

    // গ্রেডিয়েন্ট কালার সেটআপ (ডাইনামিক কালার চেঞ্জ)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val gradient = LinearGradient(
                0f, 0f, w.toFloat(), 0f,
                intArrayOf(
                    Color.parseColor("#00F2FE"), // সায়ান
                    Color.parseColor("#FF1493"), // মায়ার কিউট গোলাপি
                    Color.parseColor("#8A2BE2")  // পার্পল গ্লো
                ),
                null,
                Shader.TileMode.MIRROR
            )
            barPaint.shader = gradient
        }
    }

    fun startAnimation() {
        // কথা বললে স্পিড বেড়ে যাবে
        waveAnimator.duration = 600
    }

    fun stopAnimation() {
        // কথা শেষ হলে স্পিড আবার শান্ত হয়ে যাবে (Breathing state)
        waveAnimator.duration = 1500
        amplitude = 0f
        invalidate()
    }

    fun setAmplitude(rms: Float) {
        // সাউন্ড ইনপুট অনুযায়ী ঢেউয়ের উচ্চতা মাপবে
        amplitude = ((rms + 10f) / 20f).coerceIn(0f, 1f)
        updateBarHeights()
    }

    private fun updateBarHeights() {
        for (i in 0 until barCount) {
            // মাল্টি-লেয়ার সাইন ওয়েভ ফিজিক্স
            val wave1 = sin(i * 0.4f + phase)
            val wave2 = sin(i * 0.8f - phase * 1.5f) 
            val combinedWave = (wave1 + wave2) / 2f
            
            // কথা না বললে ২% উচ্চতায় কাঁপবে (Idle breathing)
            val idleHeight = 0.05f 
            targetHeights[i] = (idleHeight + amplitude * 0.9f * abs(combinedWave.toFloat()))
                .coerceIn(0.05f, 1f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isAnimating) return
        
        // কথা না বললেও যাতে স্মুথলি কাঁপে তার জন্য কন্টিনিউয়াস আপডেট
        if (amplitude == 0f) updateBarHeights() 

        val w = width.toFloat()
        val h = height.toFloat()
        val barWidth = w / (barCount * 2f)
        val spacing = barWidth

        for (i in 0 until barCount) {
            // ফিজিক্স ইঞ্জিন: স্মুথ ইন্টারপোলেশন
            barHeights[i] += (targetHeights[i] - barHeights[i]) * 0.25f 
            val barH = h * barHeights[i]
            val left = i * (barWidth + spacing) + spacing / 2
            val top = (h - barH) / 2
            val right = left + barWidth
            val bottom = top + barH

            // প্রতিটি দাগের জন্য আলাদা শ্যাডো/গ্লো ইফেক্ট
            val alpha = (120 + (135 * barHeights[i])).toInt().coerceIn(100, 255)
            barPaint.alpha = alpha
            
            // গোলাকার মাথাওয়ালা প্রিমিয়াম বার ড্র করা
            canvas.drawRoundRect(left, top, right, bottom, barWidth/2, barWidth/2, barPaint)
        }
    }
}

// ─── ChatMessage data class ──────────────────────────────────────────────────
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// ─── Advanced ChatAdapter (With Bounce Animation) ────────────────────────────
class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private var lastAnimatedPosition = -1 // অ্যানিমেশনের ট্র্যাক রাখার জন্য

    companion object {
        const val VIEW_USER = 0
        const val VIEW_MAWA = 1
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun clearMessages() {
        messages.clear()
        lastAnimatedPosition = -1
        notifyDataSetChanged()
    }

    fun getLastBotMessage(): String? {
        return messages.lastOrNull { !it.isUser }?.text
    }

    override fun getItemViewType(position: Int) =
        if (messages[position].isUser) VIEW_USER else VIEW_MAWA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_mawa, parent, false)
            MawaMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(msg)
            is MawaMessageViewHolder -> holder.bind(msg)
        }
        
        // 🌟 মেসেজ পপ-আপ অ্যানিমেশন (Bounce Effect)
        setAnimation(holder.itemView, position)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastAnimatedPosition) {
            viewToAnimate.translationY = 100f
            viewToAnimate.alpha = 0f
            viewToAnimate.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(OvershootInterpolator()) // বাউন্স খাবে
                .setDuration(400)
                .start()
            lastAnimatedPosition = position
        }
    }

    override fun getItemCount() = messages.size

    inner class UserMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val msgText: TextView = view.findViewById(R.id.msgText)
        private val timeText: TextView = view.findViewById(R.id.timeText)

        fun bind(msg: ChatMessage) {
            msgText.text = msg.text
            timeText.text = formatTime(msg.timestamp)
            msgText.maxLines = 100
            msgText.isSingleLine = false
        }
    }

    inner class MawaMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val msgText: TextView = view.findViewById(R.id.msgText)
        private val timeText: TextView = view.findViewById(R.id.timeText)

        fun bind(msg: ChatMessage) {
            msgText.text = msg.text
            timeText.text = formatTime(msg.timestamp)
            msgText.maxLines = 100
            msgText.isSingleLine = false
        }
    }

    private fun formatTime(ts: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(ts))
    }
}
