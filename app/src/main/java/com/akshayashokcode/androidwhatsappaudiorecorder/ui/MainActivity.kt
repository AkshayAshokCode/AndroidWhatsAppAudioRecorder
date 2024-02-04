package com.akshayashokcode.androidwhatsappaudiorecorder.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.akshayashokcode.androidwhatsappaudiorecorder.R
import com.akshayashokcode.androidwhatsappaudiorecorder.playback.AndroidAudioPlayer
import com.akshayashokcode.androidwhatsappaudiorecorder.record.AndroidAudioRecorder
import java.io.File

class MainActivity : AppCompatActivity() {

    private var recorder = AndroidAudioRecorder(this)
    private var player = AndroidAudioPlayer(this)
    private var audioFile: File? = null

    private lateinit var btnStartRecording: Button
    private lateinit var btnStopRecording: Button
    private lateinit var btnPlayAudio: Button
    private lateinit var btnStopAudio: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnStartRecording = findViewById(R.id.btnStartRecording)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        btnPlayAudio = findViewById(R.id.btnPlayAudio)
        btnStopAudio = findViewById(R.id.btnStopAudio)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            0
        )

        btnStartRecording.setOnClickListener {
            File(cacheDir, "audio.mp3").also {
                recorder.start(it)
                audioFile = it
            }
        }

        btnStopRecording.setOnClickListener {
            recorder.stop()
        }

        btnPlayAudio.setOnClickListener {
            player.playFile(audioFile ?: return@setOnClickListener)
        }

        btnStopAudio.setOnClickListener {
            player.stop()
        }
    }
}