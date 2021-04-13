package com.example.surfaceviewplayvideo

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import java.lang.Exception

open class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById()
        initView()
    }

    private var surfaceview: SurfaceView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var start: ImageButton? = null
    private var share: ImageButton? = null
    private var back: ImageButton? = null
    private var pause: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var isPlaying = false
    private var currentPosition = 0

    private fun findViewById() {
        surfaceview = findViewById<View>(R.id.surfaceView) as SurfaceView
        start = findViewById<View>(R.id.video_start) as ImageButton
        back = findViewById<View>(R.id.video_back) as ImageButton
        pause = findViewById<View>(R.id.video_pause) as ImageButton
        share = findViewById<View>(R.id.video_share) as ImageButton
        seekBar = findViewById<View>(R.id.seekBar) as SeekBar
    }

    private fun initView() {
        mediaPlayer = MediaPlayer()
        surfaceview!!.holder.setKeepScreenOn(true)
        surfaceview!!.holder.addCallback(SurfaceViewLis())
        start!!.setOnClickListener(this)
        back!!.setOnClickListener(this)
        pause!!.setOnClickListener(this)
        share!!.setOnClickListener(this)
        seekBar!!.setOnClickListener(this)
    }

    private inner class SurfaceViewLis : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceCreated(holder: SurfaceHolder) {
            // 創建 SurfaceHolder 的時候，如果存在上次播放的位置，則按照上次播放位置進行播放
            if (currentPosition > 0) {
                video_play(currentPosition)
                currentPosition = 0
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // 銷毀 SurfaceHolder 的時候，記錄當前的播放位置並停止播放
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                currentPosition = mediaPlayer!!.currentPosition
                mediaPlayer!!.stop()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.video_start -> video_play(0)
            R.id.video_pause -> pause()
            else -> {
            }
        }
    }

    /**
     * Start to play
     * @param msec: Play initial position
     */
    protected fun video_play(msec: Int) {
        // Get the video file address
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)   // Set audio stream type
            val fd = this.assets.openFd("jump2.mp4")             // Set the video source for playback
            mediaPlayer!!.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)

            // 設置顯示影像的SurfaceHolder
            mediaPlayer!!.setDisplay(surfaceview!!.holder)  //這一步是關鍵，通過setDisplay()制定用於顯示影像的SurfaceView物件
            mediaPlayer!!.prepareAsync()
            mediaPlayer!!.setOnPreparedListener {
                mediaPlayer!!.start()
                mediaPlayer!!.seekTo(msec)                  // Play according to the initial position
                seekBar!!.max = mediaPlayer!!.duration      // The maximum length of the progress bar is set to the maximum playing time of the video stream

                /** Start the thread, update the scale of the progress bar*/
                object : Thread() {
                    override fun run() {
                        try {
                            isPlaying = true
                            while (isPlaying) {
                                val current = mediaPlayer!!.currentPosition
                                seekBar!!.progress = current
                                sleep(500)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }.start()
                start!!.isEnabled = false
            }
            mediaPlayer!!.setOnCompletionListener {   // Called back after playing done
                start!!.isEnabled = true
            }
            mediaPlayer!!.setOnErrorListener {        // Replay when an error occurs
                    mp, what, extra ->
                    video_play(0)
                    isPlaying = false
                    false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Pause or Resume
     */
    private fun pause() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {mediaPlayer!!.pause()}
    }

}