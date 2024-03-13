package com.akshayashokcode.androidwhatsappaudiorecorder.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()
    private var finalAmplitudes = ArrayList<Float>()
    private var radius = 6f
    private var w = 9f
    private var d = 4f

    private var sw = 0f
    private var sh = 90f

    private var maxSpikes = 0
    init {

        paint.color = Color.parseColor("#004170")

        // sw = measuredWidth.toFloat()
        sh = dpToPx(context!!, 40f)

    }

    fun addAmplitude(amp: Float) {
        sw = measuredWidth.toFloat()
        maxSpikes = (sw / (w + d)).toInt()
        // Normalize the amplitude
        val norm = Math.min(amp.toInt() / 13, sh.toInt()).toFloat()

        // Add the new amplitude to the beginning of the list
        amplitudes.add(0, norm)

        // Remove any excess amplitudes beyond the maximum allowed spikes
        while (amplitudes.size > maxSpikes) {
            amplitudes.removeAt(amplitudes.size - 1)
        }

        // Clear the spikes list
        spikes.clear()

        // Calculate and add spikes based on the updated amplitudes list
        for (i in amplitudes.indices) {
            val left = sw - i * (w + d)
            val top = sh / 2 - amplitudes[i] / 2
            val right = left + w
            val bottom = top + amplitudes[i]
            spikes.add(RectF(left, top, right, bottom))
        }

        // Invalidate the view to trigger redraw
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        spikes.forEach{
            canvas.drawRoundRect(it, radius, radius, paint)
        }


    }

    fun clear(): ArrayList<Float>{
        val amps = amplitudes.clone() as ArrayList<Float>
        finalAmplitudes.addAll(amplitudes)
        amplitudes.clear()
        spikes.clear()
        invalidate()
        return amps

    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }
    fun getAmplitudes(): ArrayList<Float>{
        return finalAmplitudes
    }

    fun clearFinalAmplitudes(){
        finalAmplitudes.clear()
    }
}