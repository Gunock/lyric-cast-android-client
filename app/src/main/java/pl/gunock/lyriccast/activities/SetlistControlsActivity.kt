/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 4:51 PM
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.ControlsSongItemsAdapter
import pl.gunock.lyriccast.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.enums.ControlAction
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.listeners.SessionCreatedListener
import pl.gunock.lyriccast.models.SongItem

class SetlistControlsActivity : AppCompatActivity() {

    private lateinit var mRepository: LyricCastRepository

    private lateinit var mSlidePreviewView: TextView
    private lateinit var mSongTitleView: TextView

    private var mCastContext: CastContext? = null
    private var mSessionCreatedListener: SessionCreatedListener? = null
    private lateinit var mSongItemsAdapter: ControlsSongItemsAdapter

    private lateinit var mSetlistLyrics: List<String>
    private var mSongTitles: MutableMap<Int, String> = mutableMapOf()
    private var mSongStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var mCurrentLyricsPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_controls)
        setSupportActionBar(findViewById(R.id.toolbar_controls))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mRepository = (application as LyricCastApplication).repository
        mCastContext = CastContext.getSharedInstance()
        mSessionCreatedListener = SessionCreatedListener {
            sendConfigure()
            sendSlide()
        }
        mCastContext?.sessionManager!!.addSessionManagerListener(mSessionCreatedListener)

        mSlidePreviewView = findViewById(R.id.tv_setlist_slide_preview)
        mSongTitleView = findViewById(R.id.tv_current_song_title)

        val setlist: Setlist = intent.getParcelableExtra("setlist")!!
        val setlistWithSongs = runBlocking { mRepository.getSetlistWithSongs(setlist.id)!! }
        val songs = setlistWithSongs.songs
        var setlistLyricsIndex = 0

        val songLyrics = runBlocking { mRepository.getSongsWithLyrics(songs) }
            .map { songWithLyricsSections ->
                val songId = songWithLyricsSections.song.id
                val lyricsSectionsText = songWithLyricsSections.lyricsSections
                    .zip(songWithLyricsSections.crossRef)
                    .sortedBy { it.second }
                    .map { it.first.text }
                songId to lyricsSectionsText
            }.toMap()

        mSetlistLyrics = setlistWithSongs.setlistSongCrossRefs
            .zip(setlistWithSongs.songs)
            .sortedBy { it.first }
            .flatMapIndexed { index: Int, pair: Pair<SetlistSongCrossRef, Song> ->
                val songId = pair.second.songId
                val lyrics = songLyrics[songId]!!

                val indexedTitle = "[$index] ${pair.second.title}"
                mSongTitles[setlistLyricsIndex] = indexedTitle
                mSongTitles[setlistLyricsIndex + lyrics.size - 1] = indexedTitle
                mSongStartPoints[indexedTitle] = setlistLyricsIndex

                setlistLyricsIndex += lyrics.size

                return@flatMapIndexed lyrics
            }


        val songsAndCategories = runBlocking { mRepository.getSongsAndCategories(songs) }
            .map { it.song.id to it }
            .toMap()

        val setlistPresentation = setlistWithSongs.setlistSongCrossRefs
            .sorted()
            .map { it.songId }

        setupRecyclerView(setlistPresentation.map { songsAndCategories[it]!! })

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        sendConfigure()
        sendSlide()
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

    private fun setupRecyclerView(songs: List<SongAndCategory>) {
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
            onItemLongClickListener = onLongClickListener,
            onItemClickListener = onClickListener
        )

        songRecyclerView.adapter = mSongItemsAdapter
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btn_setlist_blank).setOnClickListener {
            MessageHelper.sendControlMessage(mCastContext!!, ControlAction.BLANK)
        }

        findViewById<ImageButton>(R.id.btn_setlist_prev).setOnClickListener {
            if (mCurrentLyricsPosition <= 0) {
                return@setOnClickListener
            }
            mCurrentLyricsPosition--
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_prev_song).setOnClickListener {
            val currentSongTitle = mSongTitles.entries.last {
                it.key <= mCurrentLyricsPosition
            }.value

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

            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_next).setOnClickListener {
            if (mCurrentLyricsPosition >= mSetlistLyrics.size - 1) {
                return@setOnClickListener
            }
            mCurrentLyricsPosition++
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

    private fun sendSlide() {
        if (mSongTitles.containsKey(mCurrentLyricsPosition)) {
            val songTitle = mSongTitles[mCurrentLyricsPosition]!!
            mSongTitleView.text = songTitle.replace("^\\[[0-9]+] ".toRegex(), "")
            highlightSong(songTitle)
        }

        mSlidePreviewView.text = mSetlistLyrics[mCurrentLyricsPosition]
        MessageHelper.sendContentMessage(
            mCastContext!!,
            mSetlistLyrics[mCurrentLyricsPosition]
        )
    }

    private fun selectSong(position: Int): Boolean {
        val title = mSongItemsAdapter.songItems[position].song.title
        val indexedTitle = "[$position] $title"
        mCurrentLyricsPosition = mSongStartPoints[indexedTitle]!!
        sendSlide()
        return true
    }

    private fun sendConfigure() {
        if (mCastContext == null) {
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
            mCastContext!!,
            ControlAction.CONFIGURE,
            configurationJson
        )
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }
}