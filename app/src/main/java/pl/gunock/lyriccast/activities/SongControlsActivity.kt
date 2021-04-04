/*
 * Created by Tomasz Kiljanczyk on 4/4/21 11:55 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 11:54 PM
 */

package pl.gunock.lyriccast.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.cast.CustomMediaRouteActionProvider
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
    private lateinit var sessionCreatedListener: SessionCreatedListener

    private var currentSlide = 0
    private lateinit var lyrics: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_controls)
        setSupportActionBar(findViewById(R.id.toolbar_controls))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        songTitleView = findViewById(R.id.tv_controls_song_title)
        slideNumberView = findViewById(R.id.tv_song_slide_number)
        slidePreviewView = findViewById(R.id.tv_slide_preview)

        lyrics = intent.getStringArrayExtra("lyrics")!!

        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendConfigure()
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
        castContext?.sessionManager?.removeSessionManagerListener(sessionCreatedListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_controls, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider
        if (castContext != null) {
            castActionProvider.routeSelector = castContext!!.mergedSelector
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> goToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btn_song_blank).setOnClickListener {
            sendBlank()
        }

        findViewById<ImageButton>(R.id.btn_song_prev).setOnClickListener {
            if (currentSlide <= 0) {
                return@setOnClickListener
            }
            currentSlide--
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_song_next).setOnClickListener {
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
        val backgroundColor = prefs.getString("backgroundColor", "Black")
        val fontColor = prefs.getString("fontColor", "White")

        val configurationJson = JSONObject().apply {
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

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }

}