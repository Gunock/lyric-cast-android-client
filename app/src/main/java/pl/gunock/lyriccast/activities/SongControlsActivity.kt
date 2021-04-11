/*
 * Created by Tomasz Kiljanczyk on 4/11/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 9:12 PM
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
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.cast.SessionStartedListener
import pl.gunock.lyriccast.helpers.MessageHelper


class SongControlsActivity : AppCompatActivity() {

    private var mSessionStartedListener: SessionStartedListener? = null
    private var mCurrentSlide = 0
    private lateinit var mLyrics: Array<String>

    private var mBlankOffColor: Int = Int.MIN_VALUE
    private var mBlankOnColor: Int = Int.MIN_VALUE
    private val mCurrentBlankColor: Int
        get() = if (MessageHelper.isBlanked) {
            mBlankOnColor
        } else {
            mBlankOffColor
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_controls)
        setSupportActionBar(findViewById(R.id.toolbar_controls))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mBlankOffColor = resources.getColor(R.color.green, null)
        mBlankOnColor = resources.getColor(R.color.red, null)

        findViewById<TextView>(R.id.tv_controls_song_title).text =
            intent.getStringExtra("songTitle")

        mLyrics = intent.getStringArrayExtra("lyrics")!!

        findViewById<Button>(R.id.btn_song_blank).setBackgroundColor(mCurrentBlankColor)

        setupListeners()

        mSessionStartedListener = SessionStartedListener {
            findViewById<Button>(R.id.btn_song_blank).setBackgroundColor(mBlankOffColor)
            sendConfigure()
            sendSlide()
        }

        val sessionsManager: SessionManager = CastContext.getSharedInstance()!!.sessionManager
        sessionsManager.addSessionManagerListener(mSessionStartedListener)

        if (sessionsManager.currentSession?.isConnected == true) {
            sendConfigure()
            sendSlide()
        }
        setPreview()
    }

    override fun onResume() {
        super.onResume()

        sendConfigure()
    }

    override fun onDestroy() {
        CastContext.getSharedInstance()!!.sessionManager
            .removeSessionManagerListener(mSessionStartedListener)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_controls, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider

        castActionProvider.routeSelector = CastContext.getSharedInstance()!!.mergedSelector

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
            MessageHelper.sendBlank(!MessageHelper.isBlanked)
            it.setBackgroundColor(mCurrentBlankColor)
        }

        findViewById<ImageButton>(R.id.btn_song_prev).setOnClickListener {
            if (mCurrentSlide <= 0) {
                return@setOnClickListener
            }
            mCurrentSlide--

            setPreview()
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_song_next).setOnClickListener {
            if (mCurrentSlide >= mLyrics.size - 1) {
                return@setOnClickListener
            }
            mCurrentSlide++

            setPreview()
            sendSlide()
        }
    }

    private fun sendConfigure() {
        MessageHelper.sendConfiguration(baseContext)
    }

    @SuppressLint("SetTextI18n")
    private fun setPreview() {
        findViewById<TextView>(R.id.tv_song_slide_number).text =
            "${mCurrentSlide + 1}/${mLyrics.size}"

        findViewById<TextView>(R.id.tv_slide_preview).text = mLyrics[mCurrentSlide]
    }

    private fun sendSlide() {
        MessageHelper.sendContentMessage(mLyrics[mCurrentSlide])
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }

}