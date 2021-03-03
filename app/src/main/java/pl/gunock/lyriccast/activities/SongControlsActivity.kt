/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:07 PM
 */

package pl.gunock.lyriccast.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.enums.ControlAction
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.listeners.SessionCreatedListener

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SongControlsActivity : AppCompatActivity() {

    private lateinit var songTitleView: TextView
    private lateinit var slideNumberView: TextView
    private lateinit var slidePreviewView: TextView

    private var castContext: CastContext? = null
    private var sessionCreatedListener: SessionCreatedListener? = null

    private var currentSlide = 0
    private lateinit var lyrics: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_controls)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        songTitleView = findViewById(R.id.text_view_controls_song_title)
        slideNumberView = findViewById(R.id.text_view_slide_number)
        slidePreviewView = findViewById(R.id.text_view_slide_preview)

        lyrics = intent.getStringArrayExtra("lyrics")!!

        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendSlide()
        }
        castContext?.sessionManager?.addSessionManagerListener(sessionCreatedListener)


        songTitleView.text = intent.getStringExtra("songTitle")

        setupListeners()

        sendSlide()
    }

    override fun onResume() {
        super.onResume()

        sendConfigure()
    }

    override fun onStop() {
        super.onStop()
        // TODO: Remove unused code
//        if (castContext!!.sessionManager!!.currentSession != null) {
//            castContext!!.sessionManager!!.endCurrentSession(true)
//        }
        castContext?.sessionManager?.removeSessionManagerListener(sessionCreatedListener)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.button_control_blank).setOnClickListener {
            sendBlank()
        }

        findViewById<Button>(R.id.button_prev).setOnClickListener {
            if (currentSlide <= 0) {
                return@setOnClickListener
            }
            currentSlide--
            sendSlide()
        }

        findViewById<Button>(R.id.button_next).setOnClickListener {
            if (currentSlide >= lyrics.size - 1) {
                return@setOnClickListener
            }
            currentSlide++
            sendSlide()
        }
    }

    private fun sendBlank() {
        if (castContext == null) {
            return
        }

        MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
    }

    private fun sendConfigure() {
        if (castContext == null) {
            return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val fontSize = prefs.getString("fontSize", "40")
        val backgroundColor = prefs.getString("backgroundColor", "Black")
        val fontColor = prefs.getString("fontColor", "White")

        val configurationJson = JSONObject().apply {
            put("fontSize", "${fontSize}px")
            put("backgroundColor", backgroundColor)
            put("fontColor", fontColor)
        }

        MessageHelper.sendControlMessage(
            castContext!!,
            ControlAction.CONFIGURE,
            configurationJson
        )
    }

    @SuppressLint("SetTextI18n")
    private fun sendSlide() {
        slideNumberView.text = "${currentSlide + 1}/${lyrics.size}"
        slidePreviewView.text = lyrics[currentSlide]

        if (castContext == null) {
            return
        }

        MessageHelper.sendContentMessage(
            castContext!!,
            lyrics[currentSlide]
        )
    }
}