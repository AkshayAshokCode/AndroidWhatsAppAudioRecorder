package com.akshayashokcode.androidwhatsappaudiorecorder.record

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var fileName = ""
    private var audioFile: File? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun start(dirPath: File) {

        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HH_mm_ss")
        val date = simpleDateFormat.format(Date())
        fileName = "recording_${date}.mp3"
        audioFile = File("$dirPath/$fileName")

        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile!!.path)
            Log.d("AudioRecorder", "OutPut file: ${audioFile!!.path}")
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }


            Log.d("AudioRecorder", "created Recorder")
            recorder = this
        }
    }

    override fun stop() {
        if (recorder != null) {
            recorder?.stop()
            //  recorder?.reset()
            recorder?.release()
            recorder = null
        }

    }

    override fun pause() {
        recorder?.pause()
        Log.d("AudioRecorder", "pause: pause")
    }

    override fun resume() {
        recorder?.resume()
        Log.d("AudioRecorder", "resume: resume")
    }

    override fun delete() {
        if (audioFile != null)
            audioFile!!.exists().let {
                if (it) {
                    audioFile!!.delete()
                    audioFile = null
                }
            }
    }

    override fun release() {
        recorder?.apply {
            stop()
            release()
        }
    }

    override fun getAmplitude(): Float {
        recorder?.maxAmplitude?.let {
            return it.toFloat()
        }
        return 0f
    }

    override fun getAudioFile(): File? {
        if (audioFile != null) {
            return audioFile
        }
        return null
    }

    override fun getFileName(): String {
        return fileName
    }

}