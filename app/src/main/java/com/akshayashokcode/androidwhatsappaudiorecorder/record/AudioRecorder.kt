package com.akshayashokcode.androidwhatsappaudiorecorder.record

import java.io.File

interface AudioRecorder {
    fun start(dirPath: File)
    fun stop()

    fun pause()

    fun resume()

    fun delete()

    fun release()

    fun getAmplitude(): Float

    fun getAudioFile(): File?

    fun getFileName(): String
}