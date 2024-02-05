package com.akshayashokcode.androidwhatsappaudiorecorder.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.akshayashokcode.androidwhatsappaudiorecorder.R
import com.akshayashokcode.androidwhatsappaudiorecorder.playback.AndroidAudioPlayer
import com.akshayashokcode.androidwhatsappaudiorecorder.record.AndroidAudioRecorder
import com.akshayashokcode.androidwhatsappaudiorecorder.timer.OnTimerTickListener
import com.akshayashokcode.androidwhatsappaudiorecorder.timer.Timer
import java.io.File

const val REQUEST_CODE = 200
class MainActivity : AppCompatActivity(), OnTimerTickListener {

    private var recorder = AndroidAudioRecorder(this)
    private var player = AndroidAudioPlayer(this)
    private var audioFile: File? = null

    private lateinit var btnRecord: ImageButton
    private lateinit var btnStopRecording: Button
    private lateinit var btnPlayAudio: Button
    private lateinit var btnStopAudio: Button
    private lateinit var tvTimer: TextView

    private lateinit var timer: Timer

    private var isRecording = false
    private var isPaused = false

    private lateinit var vibrator: Vibrator

    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnRecord = findViewById(R.id.btnRecord)
//        btnStopRecording = findViewById(R.id.btnStopRecording)
//        btnPlayAudio = findViewById(R.id.btnPlayAudio)
//        btnStopAudio = findViewById(R.id.btnStopAudio)

        timer = Timer(this)

        /* vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }*/

        // TODO handle API level
      //  vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))

        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        if(!permissionGranted) {
            ActivityCompat.requestPermissions(
                this,
               permissions,
                REQUEST_CODE
            )
        }

        btnRecord.setOnClickListener {
            when{
                isPaused -> resumeRecorder()
                isRecording -> pauseRecorder()
                else -> startRecording()
            }
        }


//
//        btnStartRecording.setOnClickListener {
//            File(cacheDir, "audio.mp3").also {
//                recorder.start(it)
//                audioFile = it
//            }
//        }
//
//        btnStopRecording.setOnClickListener {
//            recorder.stop()
//        }
//
//        btnPlayAudio.setOnClickListener {
//            player.playFile(audioFile ?: return@setOnClickListener)
//        }
//
//        btnStopAudio.setOnClickListener {
//            player.stop()
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecording(){
        if(!permissionGranted){
            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_CODE
            )
            return
        }
        recorder.start(externalCacheDir?.absolutePath +"/")

        btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false

        timer.start()
    }

    private fun pauseRecorder(){
        recorder.pause()
        isPaused = true
        btnRecord.setImageResource(R.drawable.ic_record)
        timer.pause()
    }

    private fun resumeRecorder(){
        recorder.resume()
        isPaused = true
        btnRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun stopRecorder(){
        timer.stop()
    }

    override fun onTimerTick(duration: String) {
        tvTimer.text = duration
    }
}