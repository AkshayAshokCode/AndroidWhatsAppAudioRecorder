package com.akshayashokcode.androidwhatsappaudiorecorder.record

import java.io.File

interface AudioRecorder {
    fun start(dirPath: String)
    fun stop()

    fun pause()

    fun resume()

    fun getAmplitude(): Float
}