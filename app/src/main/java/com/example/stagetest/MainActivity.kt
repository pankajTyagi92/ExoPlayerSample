package com.example.stagetest

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.example.stagetest.databinding.ActivityMainBinding


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private val playbackStateListener: Player.Listener = playbackListener()
    private var player: Player? = null
    private var playWhenReady = true
    private var itemIndex = 0
    private var playbackPosition = 0L
    private val url1="https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.m3u8"
    private val url2="https://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)
        setClickListener()
    }

    /*
    *
    * will set click listener for fullscreen button and resolution floating button
    *
    * */
    private fun setClickListener() {
        viewBinding.resolutionButton.setOnClickListener {
            //  trackSeleccion()
            TrackSelectionDialogBuilder(
                this,
                getString(R.string.select_resolution),
                player!!,
                C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            ).build().show()
        }
        viewBinding.videoView.setFullscreenButtonClickListener {
            Log.d("pankaj full ", "${it}")

            if (it) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

                /*val params = viewBinding.videoView.getLayoutParams()
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = (200 * applicationContext.resources.displayMetrics.density).toInt()
                viewBinding.videoView.setLayoutParams(params)*/
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                /* val params = viewBinding.videoView.getLayoutParams()
                 params.width = ViewGroup.LayoutParams.MATCH_PARENT
                 params.height = ViewGroup.LayoutParams.MATCH_PARENT
                 viewBinding.videoView.setLayoutParams(params)*/

            }
        }
    }

    public override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (player == null) {
            initializePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        // ExoPlayer implements the Player interface
        player = ExoPlayer.Builder(this)
            .build()
            .also { it ->
                viewBinding.videoView.player = it

                // Update the track selection parameters to only pick standard definition tracks
                it.trackSelectionParameters = it.trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSizeSd()
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(url1)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
                val mediaItem2 = MediaItem.Builder()
                    .setUri(url2)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()

                it.setMediaItems(listOf(mediaItem, mediaItem2), itemIndex, playbackPosition)
                it.playWhenReady = playWhenReady
                it.addListener(playbackStateListener)
                it.prepare()
            }

    }

    /*
    *
    * function to release resource or to remove listener
    *
    * */
    private fun releasePlayer() {
        player?.let { player ->
            playbackPosition = player.currentPosition
            itemIndex = player.currentMediaItemIndex
            playWhenReady = player.playWhenReady
            player.removeListener(playbackStateListener)
            player.release()
        }
        player = null
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBinding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /*
    * Player Listener start
    *
    *
    * */
    private fun playbackListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    Log.d("pankaj ", "ExoPlayer.STATE_IDLE")
                    viewBinding.progressBar.visibility = View.GONE
                }

                ExoPlayer.STATE_BUFFERING -> {
                    Log.d("pankaj ", "ExoPlayer.STATE_BUFFERING")
                    viewBinding.progressBar.visibility=View.VISIBLE
                }

                ExoPlayer.STATE_READY -> {
                    Log.d("pankaj ", "ExoPlayer.STATE_READY")
                    viewBinding.progressBar.visibility=View.GONE
                    /* trackSelector.()?.let {
                         qualityList = it
                         setUpQualityList()*/
                }

                ExoPlayer.STATE_ENDED -> {
                    Log.d("pankaj ", "ExoPlayer.STATE_ENDED")
                    viewBinding.progressBar.visibility=(View.GONE)
                }

                else -> {
                    Log.d("pankaj ", "ExoPlayer.STATE_UNKNOWN")
                    viewBinding.progressBar.visibility=View.GONE
                }
            }
            // Log.d("pankaj ", "changed state to $stateString")
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            showErrorDialog( error.errorCodeName)
            /*when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {Log.e(
                    TAG,
                    "TYPE_SOURCE: " + error.errorCodeName
                )
                    showErrorDialog( error.errorCodeName)
                }

                PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> {
                    showErrorDialog( error.errorCodeName)
                    Log.e(
                        TAG,
                        "TYPE_RENDERER: " + error.errorCodeName
                    )
                }
                PlaybackException.ERROR_CODE_UNSPECIFIED->{

                    Log.e(
                    TAG,
                    "TYPE_UNEXPECTED: " + error.errorCodeName
                )
            }}*/

        }

        override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
            super.onTrackSelectionParametersChanged(parameters)
            Log.e("VideoScreen", "onTrackSelectionParametersChanged")

        }


    }

    /*
    * Player listener end
    * */


    /*function to show error dialog*/
    fun showErrorDialog(message: String) {
        AlertDialog.Builder(this) //set icon
            .setIcon(android.R.drawable.ic_dialog_alert) //set title
            .setTitle(getString(R.string.error)) //set message
            .setMessage(message) //set positive button
            .setPositiveButton(
                getString(R.string.retry),
                 { dialogInterface, i ->
                   player!!.prepare()
                    dialogInterface.dismiss()
                }) //set negative button
            .setNegativeButton(
                getString(R.string.cancel),
                { dialogInterface, i -> //set what should happen when negative button is clicked
                    dialogInterface.dismiss()
                })
            .show()
    }
}


