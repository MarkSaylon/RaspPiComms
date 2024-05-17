package com.example.rasppicomms

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity(), Player.Listener {
    private lateinit var sendButton: Button
    private lateinit var textView: TextView

    private val client = OkHttpClient()

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendButton = findViewById(R.id.sendButton)
        textView = findViewById(R.id.textView)
        playerView = findViewById(R.id.playerView)

        sendButton.setOnClickListener {
            sendMessage("hi")
            showVideoFeed()
        }
    }

    private fun sendMessage(message: String) {
        val requestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), message)
        val request = Request.Builder()
            .url("http://192.168.1.9:8080/send_message")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SocketCommunication", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d("SocketCommunication", "Received response: $responseBody")
                runOnUiThread {
                    textView.text = responseBody
                }
            }
        })
    }

    private fun showVideoFeed() {
        val streamUrl = "http://192.168.1.9:8080/stream.ts"
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        player = ExoPlayer.Builder(this).build().also {
            it.addListener(this)
            playerView.player = it
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

}