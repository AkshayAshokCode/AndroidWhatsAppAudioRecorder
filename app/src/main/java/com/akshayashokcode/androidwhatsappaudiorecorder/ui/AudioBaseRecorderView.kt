package com.akshayashokcode.androidwhatsappaudiorecorder.ui

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.akshayashokcode.androidwhatsappaudiorecorder.R
import com.akshayashokcode.androidwhatsappaudiorecorder.record.AndroidAudioRecorder
import com.akshayashokcode.androidwhatsappaudiorecorder.timer.OnTimerTickListener
import com.akshayashokcode.androidwhatsappaudiorecorder.timer.Timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AudioBaseRecorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener,
    OnTimerTickListener {
    private var recorder = AndroidAudioRecorder(context)

    val PERMISSION_RECORD_13 =
        arrayOf("android.permission.RECORD_AUDIO", "android.permission.READ_MEDIA_AUDIO")

    val PERMISSION_RECORD_SIMPLE_STORAGE = arrayOf(
        "android.permission.RECORD_AUDIO", "android" +
                ".permission.READ_EXTERNAL_STORAGE"
    )


    val PERMISSION_RECORD = arrayOf(
        "android.permission.RECORD_AUDIO", "android" +
                ".permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"
    )
    //  private lateinit var player: AndroidAudioPlayer

    private var mediaPlayer: MediaPlayer? = null

    private var timer: Timer = Timer(this)
    private var isRecording = false
    private var isPaused = false
    private lateinit var audioRecorderStatusListener: AudioRecorderStatusListener

    private lateinit var llPlayerView: RelativeLayout
    private lateinit var playerControlBtn: ImageView
    private lateinit var playerSeekbar: SeekBar
    private lateinit var tvPlayerTimer: TextView
    private lateinit var tvPlayerTotalTime: TextView
    private var isPlaying = false

    private lateinit var llRecorderView: LinearLayout
    private lateinit var waveformView: WaveformView
    private lateinit var flAudioSend: FrameLayout
    private lateinit var ivAudioSend: ImageView
    private lateinit var ivAudioControl: ImageView
    private lateinit var ivAudioDelete: ImageView

    private lateinit var tvTimer: TextView

    private val minRecordTime = 1500L

    private var job: Job? = null

    init {
        init(context)
    }

    private fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_chat_audio_editor, this, true)


        llPlayerView = findViewById(R.id.llPlayerView)
        playerControlBtn = findViewById(R.id.playerControlBtn)
        playerSeekbar = findViewById(R.id.playerSeekbar)
        tvPlayerTimer = findViewById(R.id.tvPlayerTimer)
        tvPlayerTotalTime = findViewById(R.id.tvPlayerTotalTime)

        llRecorderView = findViewById(R.id.llRecorderView)
        waveformView = findViewById(R.id.waveformView)
        tvTimer = findViewById(R.id.tvTimer)

        flAudioSend = findViewById(R.id.flAudioSend)
        ivAudioSend = findViewById(R.id.ivAudioSend)
        ivAudioControl = findViewById(R.id.ivAudioMic)
        ivAudioDelete = findViewById(R.id.ivAudioDelete)

        audioRecorderStatusListener = context as AudioRecorderStatusListener
        flAudioSend.setOnClickListener(SafeClickListener(this))
        ivAudioSend.setOnClickListener(SafeClickListener(this))
        ivAudioControl.setOnClickListener(SafeClickListener(this))
        ivAudioDelete.setOnClickListener(SafeClickListener(this))
        playerControlBtn.setOnClickListener(SafeClickListener(this))


        playerSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (mediaPlayer != null) {
                        //TODO Enhancement needed here to give user more control over seekbar
                        // But to keep the consistency with other audio players of the App, there will be no change
                        mediaPlayer!!.seekTo(0)
                        tvPlayerTimer.text = timer.format(mediaPlayer!!.currentPosition.toLong())
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
        })

    }

    fun start(permissionChecked: Boolean) {
        if (permissionChecked) {
            startRecording()
        } else {
            val permissions: Array<String> = if (Build.VERSION.SDK_INT > 32) {
                PERMISSION_RECORD_13
            } else if (Build.VERSION.SDK_INT >= 30) {
               PERMISSION_RECORD_SIMPLE_STORAGE
            } else {
               PERMISSION_RECORD
            }
            if ((context as ChatNewActivity).checkPermission(
                    context as ChatNewActivity,
                    permissions
                )
            ) {
                startRecording()

            } else {
                (context as ChatNewActivity).requestAppPermissions(
                    permissions,
                    100
                )
            }
        }


    }

    override fun onClick(v: View) {
        when (v) {
            flAudioSend, ivAudioSend -> {
                try {
                    if(timer.duration > minRecordTime){
                        stopRecorder()
                        if (recorder.getAudioFile() != null) {
                            audioRecorderStatusListener.onAudioRecorded(createAudioFile(recorder.getAudioFile()!!))
                        }
                        resetView()
                        resetPlayer()
                    }
                } catch (e: Exception) {

                }


            }

            ivAudioControl -> {
                when {
                    isPaused -> {
                        resumeRecorder()
                    }

                    isRecording -> {
                        if(timer.duration > minRecordTime) {
                            pauseRecorder()
                        }
                    }
                    else -> start(false)
                }
            }

            ivAudioDelete -> {
                try {
                    stopRecorder()
                    recorder.delete()
                    resetView()
                    resetPlayer()
                    audioRecorderStatusListener.onAudioRecorded(null)
                } catch (e: Exception) {

                }

            }

            playerControlBtn -> {
                if (isPlaying) {
                    playerControlBtn.setImageResource(R.drawable.chat_audio_player_play)
                    if (mediaPlayer != null) {
                        mediaPlayer!!.pause()
                    }
                } else {
                    if (mediaPlayer != null) {
                        mediaPlayer!!.start()

                        job = CoroutineScope(Dispatchers.Main).launch {
                            if (mediaPlayer != null) {
                                updateSeekBar()
                            }
                        }
                        playerControlBtn.setImageResource(R.drawable.chat_audio_player_pause)
                    }
                }
                isPlaying = !isPlaying

            }
        }
    }

    private fun startRecording() {
        setPlayerView(false)
        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MUSIC
        )
        recorder.start(path)
        timer.start()

        ivAudioControl.setImageResource(R.drawable.chat_audio_pause)
        isRecording = true
        isPaused = false
        isSendingEnabled(true)
    }

    fun pauseRecorder() {
        timer.pause()
        recorder.pause()
        isPaused = true
        ivAudioControl.setImageResource(R.drawable.chat_audio_mic)
        isSendingEnabled(true)
        setPlayerView(true)
    }

    private fun setPlayerView(enabled: Boolean) {
        if (enabled) {
            llPlayerView.visibility = View.VISIBLE
            llRecorderView.visibility = View.GONE
            setAudioToPlayer()
        } else {
            resetPlayer()
            llPlayerView.visibility = View.GONE
            llRecorderView.visibility = View.VISIBLE
        }
    }

    private fun setAudioToPlayer() {
        if (recorder.getAudioFile() != null) {
            mediaPlayer = MediaPlayer.create(context, recorder.getAudioFile()!!.toUri())

            mediaPlayer?.setOnPreparedListener {
                playerSeekbar.max = mediaPlayer!!.duration
                tvPlayerTotalTime.text = timer.format(mediaPlayer!!.duration.toLong())
            }
            mediaPlayer?.setOnCompletionListener {
                // mediaPlayer!!.prepare()
            }
        }
    }

    private suspend fun updateSeekBar() {
        while (job?.isActive == true) {
            playerSeekbar.progress = mediaPlayer!!.currentPosition
            tvPlayerTimer.text = timer.format(mediaPlayer!!.currentPosition.toLong())
            if (!mediaPlayer!!.isPlaying) {
                playerControlBtn.setImageResource(R.drawable.chat_audio_player_play)
                tvPlayerTimer.text = "00:00"
                playerSeekbar.progress = 0
                mediaPlayer!!.seekTo(0)
                isPlaying = false
                job?.cancel()
            }
            delay(50)
        }
    }


    private fun resumeRecorder() {
        val permissions: Array<String> = if (Build.VERSION.SDK_INT > 32) {
            PERMISSION_RECORD_13
        } else if (Build.VERSION.SDK_INT >= 30) {
           PERMISSION_RECORD_SIMPLE_STORAGE
        } else {
           PERMISSION_RECORD
        }
        if ((context as ChatNewActivity).checkPermission(
                context as ChatNewActivity,
                permissions
            )
        ) {
            setPlayerView(false)
            recorder.resume()
            isPaused = false
            ivAudioControl.setImageResource(R.drawable.chat_audio_pause)
            timer.start()
            isSendingEnabled(true)

        } else {
            (context as ChatNewActivity).requestAppPermissions(
                permissions,
               100
            )
        }

    }

    private fun stopRecorder() {
        try {
            timer.stop()
            recorder.stop()
            recorder.release()
        } catch (e: Exception) {

        }

        isPaused = false
        isRecording = false
        tvTimer.text = "00:00"
        ivAudioControl.setImageResource(R.drawable.chat_audio_mic)
        isSendingEnabled(false)
        resetView()
    }


    override fun onTimerTick(duration: String) {
        (context as Activity).runOnUiThread {
            tvTimer.text = duration
            waveformView.addAmplitude(recorder.getAmplitude())
        }
    }

    private fun createAudioFile(file: File): AudioFile {
        val parentFile = file.parentFile
        var audioFile = AudioFile()
        audioFile.setmTitle(recorder.getFileName())
        if (parentFile != null) {
            audioFile.setmAlbum(parentFile.name)
        }
        val player = MediaPlayer()
        try {
            player.setDataSource(file.path)
            player.prepare()
            audioFile.setmDuration(player.duration.toLong())
        } catch (ignored: Exception) {
        } finally {
            player.release()
        }
        audioFile.setmData(file.path)
        audioFile.setmArtist("<unknown>")

        return audioFile
    }

    fun resetOnDestroy() {
        resetView()
        resetPlayer()
    }

    private fun resetView() {
        try {
            waveformView.clear()
            tvTimer.text = "00:00"
            timer?.stop()
            recorder?.stop()
            recorder?.release()
        }catch (e:Exception){

        }

    }

    private fun resetPlayer() {
        try {
            tvPlayerTimer.text = "00:00"
            tvPlayerTotalTime.text = "00:00"
            playerSeekbar.progress = 0
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            job?.cancel()
            playerControlBtn.setImageResource(R.drawable.chat_audio_player_play)
        }catch (e:Exception){

        }

    }

    private fun isSendingEnabled(enabled: Boolean) {
        if (enabled) {
            flAudioSend.isClickable = true
            ivAudioSend.isEnabled = true
            flAudioSend.backgroundTintList =
                resources.getColorStateList(R.color.public_blue, resources.newTheme())
            ivAudioSend.setColorFilter(
                ContextCompat.getColor(context, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            flAudioSend.isClickable = false
            ivAudioSend.isEnabled = false
            flAudioSend.backgroundTintList =
                resources.getColorStateList(R.color.background_gray, resources.newTheme())
            ivAudioSend.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.color_switch_disabled
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )

        }
    }

    interface AudioRecorderStatusListener {
        fun onAudioRecorded(audioFile: AudioFile?)
    }
}