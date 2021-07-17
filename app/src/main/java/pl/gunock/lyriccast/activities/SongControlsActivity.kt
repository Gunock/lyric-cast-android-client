/*
 * Created by Tomasz Kiljanczyk on 17/07/2021, 11:19
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:05
 */

package pl.gunock.lyriccast.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.cast.SessionStartedListener
import pl.gunock.lyriccast.databinding.*
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.SongDocument
import pl.gunock.lyriccast.extensions.loadAd
import pl.gunock.lyriccast.helpers.MessageHelper


class SongControlsActivity : AppCompatActivity() {

    private lateinit var mBinding: ContentSongControlsBinding

    private var mSessionStartedListener: SessionStartedListener? = null
    private var mCurrentSlide = 0
    private lateinit var mLyrics: List<String>

    private var mBlankOnColor: Int = Int.MIN_VALUE
    private var mBlankOffColor: Int = Int.MIN_VALUE
    private val mCurrentBlankColor: Int
        get() = if (MessageHelper.isBlanked.value!!) {
            mBlankOffColor
        } else {
            mBlankOnColor
        }

    private lateinit var mBlankOffText: String
    private lateinit var mBlankOnText: String
    private val mCurrentBlankText: String
        get() = if (MessageHelper.isBlanked.value!!) {
            mBlankOffText
        } else {
            mBlankOnText
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivitySongControlsBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mBinding = ContentSongControlsBinding.bind(rootBinding.contentSongControls.root)

        mBinding.advSongControls.loadAd()

        mBlankOffText = getString(R.string.controls_off)
        mBlankOnText = getString(R.string.controls_on)
        mBlankOnColor = getColor(R.color.green)
        mBlankOffColor = getColor(R.color.red)

        val songId: ObjectId = intent.getSerializableExtra("songId")!! as ObjectId

        val databaseViewModel =
            DatabaseViewModel.Factory(resources).create(DatabaseViewModel::class.java)
        val song: SongDocument = databaseViewModel.getSong(songId)!!
        databaseViewModel.close()

        mBinding.tvControlsSongTitle.text = song.title

        mLyrics = song.lyricsList

        mBinding.btnSongBlank.setBackgroundColor(mCurrentBlankColor)
        mBinding.btnSongBlank.text = mCurrentBlankText

        setupListeners()

        mSessionStartedListener = SessionStartedListener {
            sendConfigure()
            sendSlide()
        }

        val sessionsManager: SessionManager = CastContext.getSharedInstance()!!.sessionManager
        sessionsManager.addSessionManagerListener(mSessionStartedListener!!)

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
            .removeSessionManagerListener(mSessionStartedListener!!)

        MessageHelper.isBlanked.removeObservers(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_controls, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider

        castActionProvider.routeSelector = CastContext.getSharedInstance()!!.mergedSelector!!

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> goToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        mBinding.btnSongBlank.setOnClickListener {
            MessageHelper.sendBlank(!MessageHelper.isBlanked.value!!)
        }

        MessageHelper.isBlanked.observe(this) {
            mBinding.btnSongBlank.setBackgroundColor(mCurrentBlankColor)
            mBinding.btnSongBlank.text = mCurrentBlankText
        }

        mBinding.btnSongPrev.setOnClickListener {
            if (mCurrentSlide <= 0) {
                return@setOnClickListener
            }
            mCurrentSlide--

            setPreview()
            sendSlide()
        }

        mBinding.btnSongNext.setOnClickListener {
            if (mCurrentSlide >= mLyrics.size - 1) {
                return@setOnClickListener
            }
            mCurrentSlide++

            setPreview()
            sendSlide()
        }
    }

    private fun sendConfigure() {
        MessageHelper.sendConfiguration()
    }

    @SuppressLint("SetTextI18n")
    private fun setPreview() {
        mBinding.tvSongSlideNumber.text =
            "${mCurrentSlide + 1}/${mLyrics.size}"

        mBinding.tvSlidePreview.text = mLyrics[mCurrentSlide]
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