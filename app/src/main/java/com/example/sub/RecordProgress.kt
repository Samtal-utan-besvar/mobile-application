package com.example.sub

import android.animation.TimeInterpolator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar


class RecordProgress(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var progressCircle : CircularProgressBar
    private var button : ImageButton

    private var interval : Long = 5000
    private var records = false

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.record_layout, this, true)
        progressCircle = view.findViewById(R.id.circularProgressBar)
        button = view.findViewById(R.id.recordButton)
    }

    private fun runAnimation() {
        if (records) {
            progressCircle.progress = 0f
            progressCircle.setProgressWithAnimation(100f, interval, CustomInterpolator())
        }
    }


    override fun setOnTouchListener(l: OnTouchListener?) {
        button.setOnTouchListener(l)
    }

    fun start(interval: Long) {
        records = true
        this.interval = interval
        runAnimation()
    }


    fun restart() {
        Handler(Looper.getMainLooper()).post {
            runAnimation()
        }
    }


    fun stop() {
        records = false
        val duration = (progressCircle.progress * 3).toLong()
        progressCircle.setProgressWithAnimation(0f, duration)
    }


    inner class CustomInterpolator : TimeInterpolator {
        override fun getInterpolation(p0: Float): Float {
            return p0
        }

    }

}