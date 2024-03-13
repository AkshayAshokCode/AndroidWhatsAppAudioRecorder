package com.akshayashokcode.androidwhatsappaudiorecorder.timer

import android.util.Log
import java.util.Timer
import java.util.TimerTask

class Timer(private val listener: OnTimerTickListener) {

    var duration = 0L
    private var delay = 100L
    private var timer: Timer? = null
    fun start() {
        timer = Timer()
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                duration += delay
                listener.onTimerTick(format(duration))
            }
        }, 0, 100)

        Log.d("AudioRecorder", "duration: $duration")
    }

    fun pause() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

    fun stop() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
        duration = 0L
    }

    fun format(duration: Long): String {
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60))

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}
