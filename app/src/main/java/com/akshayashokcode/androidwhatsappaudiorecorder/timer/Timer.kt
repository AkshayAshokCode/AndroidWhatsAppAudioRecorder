package com.akshayashokcode.androidwhatsappaudiorecorder.timer

import android.os.Handler
import android.os.Looper

class Timer(listener: OnTimerTickListener) {
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var duration = 0L
    private var delay = 100L

    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerTick(format(duration))
        }
    }

    fun start() {
        handler.postDelayed(runnable, delay)
    }

    fun pause() {
        handler.removeCallbacks(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    private fun format(duration: Long): String {
      //  val millis = duration % 1000
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60))

        var formatted = if(hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        }else{
            "%02d:%02d".format(minutes, seconds)
        }

        return formatted
    }
}