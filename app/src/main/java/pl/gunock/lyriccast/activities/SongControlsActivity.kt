/*
 * Created by Tomasz Kiljanczyk on 4/9/21 5:36 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 5:36 PM
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
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.enums.ControlAction
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.listeners.SessionCreatedListener
import pl.gunock.lyriccast.models.LyricCastSettings

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SongControlsActivity : AppCompatActivity() {

    private lateinit var mSongTitleView: TextView
    private lateinit var mSlideNumberView: TextView
    private lateinit var mSlidePreviewView: TextView

    private var mCastContext: CastContext? = null
    private lateinit var mSessionCreatedListener: SessionCreatedListener

    private var mCurrentSlide = 0
    private lateinit var mLyrics: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_controls)
        setSupportActionBar(findViewById(R.id.toolbar_controls))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mSongTitleView = findViewById(R.id.tv_controls_song_title)
        mSlideNumberView = findViewById(R.id.tv_song_slide_number)
        mSlidePreviewView = findViewById(R.id.tv_slide_preview)

        mLyrics = intent.getStringArrayExtra("lyrics")!!

        mCastContext = CastContext.getSharedInstance()
        mSessionCreatedListener = SessionCreatedListener {
            sendConfigure()
            sendSlide()
        }
        mCastContext?.sessionManager?.addSessionManagerListener(mSessionCreatedListener)

        mSongTitleView.text = intent.getStringExtra("songTitle")

        setupListeners()

        sendSlide()
    }

    override fun onResume() {
        super.onResume()

        sendConfigure()
    }

    override fun onStop() {
        super.onStop()
        mCastContext?.sessionManager?.removeSessionManagerListener(mSessionCreatedListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_controls, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider
        if (mCastContext != null) {
            castActionProvider.routeSelector = mCastContext!!.mergedSelector
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
            if (mCurrentSlide <= 0) {
                return@setOnClickListener
            }
            mCurrentSlide--
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_song_next).setOnClickListener {
            if (mCurrentSlide >= mLyrics.size - 1) {
                return@setOnClickListener
            }
            mCurrentSlide++
            sendSlide()
        }
    }

    private fun sendBlank() {
        if (mCastContext == null) {
            return
        }

        MessageHelper.sendControlMessage(mCastContext!!, ControlAction.BLANK)
    }

    private fun sendConfigure() {
        if (mCastContext == null) {
            return
        }

        val configurationJson = LyricCastSettings(baseContext).getCastConfigurationJson()

        MessageHelper.sendControlMessage(
            mCastContext!!,
            ControlAction.CONFIGURE,
            configurationJson
        )
    }

    @SuppressLint("SetTextI18n")
    private fun sendSlide() {
        mSlideNumberView.text = "${mCurrentSlide + 1}/${mLyrics.size}"
        mSlidePreviewView.text = mLyrics[mCurrentSlide]

        if (mCastContext == null) {
            return
        }

        MessageHelper.sendContentMessage(
            mCastContext!!,
            mLyrics[mCurrentSlide]
        )
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }

}