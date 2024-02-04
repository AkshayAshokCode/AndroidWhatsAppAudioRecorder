package com.akshayashokcode.androidwhatsappaudiorecorder.record

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()

    //TODO: Add resume & pause methods
}