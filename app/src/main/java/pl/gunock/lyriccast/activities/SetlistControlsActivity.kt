/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 12:46 AM
 */

package pl.gunock.lyriccast.activities

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.ControlsSongItemsAdapter
import pl.gunock.lyriccast.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.cast.SessionStartedListener
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.SetlistDocument
import pl.gunock.lyriccast.datamodel.documents.SongDocument
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SongItem

class SetlistControlsActivity : AppCompatActivity() {

    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.Factory(resources)
    }

    private var mSessionStartedListener: SessionStartedListener? = null
    private lateinit var mSongItemsAdapter: ControlsSongItemsAdapter

    private lateinit var mSetlistLyrics: List<String>
    private var mSongTitles: MutableMap<Int, String> = mutableMapOf()
    private var mSongStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var mCurrentLyricsPosition: Int = 0

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

        setContentView(R.layout.activity_setlist_controls)
        setSupportActionBar(findViewById(R.id.toolbar_controls))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mBlankOffColor = resources.getColor(R.color.green, null)
        mBlankOnColor = resources.getColor(R.color.red, null)

        findViewById<Button>(R.id.btn_setlist_blank).setBackgroundColor(mCurrentBlankColor)

        mSessionStartedListener = SessionStartedListener {
            findViewById<Button>(R.id.btn_setlist_blank).setBackgroundColor(mBlankOffColor)
            sendConfigure()
            sendSlide()
        }

        CastContext.getSharedInstance()!!.sessionManager.addSessionManagerListener(
            mSessionStartedListener
        )

        val setlist: SetlistDocument = setupLyrics()

        setupRecyclerView(setlist.presentation)

        setupListeners()
        setPreview()
    }

    override fun onResume() {
        super.onResume()

        sendConfigure()
        sendSlide()
    }

    override fun onDestroy() {
        CastContext.getSharedInstance()!!.sessionManager.removeSessionManagerListener(
            mSessionStartedListener
        )
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

    private fun setupLyrics(): SetlistDocument {
        val setlistId: ObjectId = intent.getSerializableExtra("setlistId")!! as ObjectId
        val setlist: SetlistDocument = mDatabaseViewModel.allSetlists
            .where()
            .equalTo("id", setlistId)
            .findFirst()!!

        var setlistLyricsIndex = 0
        mSetlistLyrics = setlist.presentation
            .flatMapIndexed { index: Int, song: SongDocument ->
                val lyrics = song.lyricsList

                val indexedTitle = "[$index] ${song.title}"
                mSongTitles[setlistLyricsIndex] = indexedTitle
                mSongTitles[setlistLyricsIndex + lyrics.size - 1] = indexedTitle
                mSongStartPoints[indexedTitle] = setlistLyricsIndex

                setlistLyricsIndex += lyrics.size

                return@flatMapIndexed lyrics
            }

        return setlist
    }

    private fun setupRecyclerView(songs: List<SongDocument>) {
        val songRecyclerView: RecyclerView = findViewById(R.id.rcv_songs)
        songRecyclerView.setHasFixedSize(true)
        songRecyclerView.layoutManager = LinearLayoutManager(baseContext)

        val onLongClickListener =
            LongClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                songRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                selectSong(position)
            }

        val onClickListener =
            ClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                songRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                selectSong(position)
            }

        val songItemList: List<SongItem> = songs.map { song -> SongItem(song) }

        mSongItemsAdapter = ControlsSongItemsAdapter(
            this,
            songItemList,
            mOnItemLongClickListener = onLongClickListener,
            mOnItemClickListener = onClickListener
        )

        songRecyclerView.adapter = mSongItemsAdapter
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btn_setlist_blank).setOnClickListener {
            MessageHelper.sendBlank(!MessageHelper.isBlanked)
            it.setBackgroundColor(mCurrentBlankColor)
        }

        findViewById<ImageButton>(R.id.btn_setlist_prev).setOnClickListener {
            if (mCurrentLyricsPosition <= 0) {
                return@setOnClickListener
            }
            mCurrentLyricsPosition--

            setPreview()
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_prev_song).setOnClickListener {
            val currentSongTitle = mSongTitles.entries.last {
                it.key <= mCurrentLyricsPosition
            }.value

            @Suppress("LiftReturnOrAssignment")
            if (mSongTitles.values.toList()[0] == currentSongTitle) {
                mCurrentLyricsPosition = 0
            } else {

                val previousSongTitle: String = mSongTitles.entries.lastOrNull {
                    it.key < mCurrentLyricsPosition && it.value != currentSongTitle
                }?.value ?: return@setOnClickListener

                val previousSongStartIndex: Int = mSongTitles.entries.first {
                    it.value == previousSongTitle
                }.key
                mCurrentLyricsPosition = previousSongStartIndex
            }

            setPreview()
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_next).setOnClickListener {
            if (mCurrentLyricsPosition >= mSetlistLyrics.size - 1) {
                return@setOnClickListener
            }
            mCurrentLyricsPosition++

            setPreview()
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_next_song).setOnClickListener {
            val currentSongTitle = mSongTitles.entries.last {
                it.key <= mCurrentLyricsPosition
            }.value

            val nextSongIndex: Int = mSongTitles.entries.firstOrNull {
                it.key > mCurrentLyricsPosition && it.value != currentSongTitle
            }?.key ?: return@setOnClickListener

            mCurrentLyricsPosition = nextSongIndex

            setPreview()
            sendSlide()
        }
    }

    private fun highlightSong(title: String) {
        val songItemPosition: Int = mSongStartPoints.keys
            .indexOfFirst { songTitle -> songTitle == title }

        mSongItemsAdapter.songItems.forEachIndexed { index, songItem ->
            songItem.highlight.value = index == songItemPosition
        }

        findViewById<RecyclerView>(R.id.rcv_songs).run {
            scrollToPosition(songItemPosition)
            postInvalidate()
        }
    }

    private fun setPreview() {
        if (mSongTitles.containsKey(mCurrentLyricsPosition)) {
            val songTitle = mSongTitles[mCurrentLyricsPosition]!!
            findViewById<TextView>(R.id.tv_current_song_title).text =
                songTitle.replace("^\\[[0-9]+] ".toRegex(), "")

            highlightSong(songTitle)
        }

        findViewById<TextView>(R.id.tv_setlist_slide_preview).text =
            mSetlistLyrics[mCurrentLyricsPosition]
    }

    private fun selectSong(position: Int): Boolean {
        val title = mSongItemsAdapter.songItems[position].song.title
        val indexedTitle = "[$position] $title"
        mCurrentLyricsPosition = mSongStartPoints[indexedTitle]!!

        setPreview()
        sendSlide()
        return true
    }

    private fun sendSlide() {
        MessageHelper.sendContentMessage(
            mSetlistLyrics[mCurrentLyricsPosition]
        )
    }

    private fun sendConfigure() {
        MessageHelper.sendConfiguration(baseContext)
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }
}