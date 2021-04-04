/*
 * Created by Tomasz Kiljanczyk on 4/4/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 11:50 PM
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

    private lateinit var repository: LyricCastRepository

    private lateinit var slidePreviewView: TextView
    private lateinit var songTitleView: TextView

    private var castContext: CastContext? = null
    private var sessionCreatedListener: SessionCreatedListener? = null
    private lateinit var songItemsAdapter: ControlsSongItemsAdapter

    private lateinit var setlistLyrics: List<String>
    private var songTitles: MutableMap<Int, String> = mutableMapOf()
    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var currentLyricsPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_controls)
        setSupportActionBar(findViewById(R.id.toolbar_controls))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        repository = (application as LyricCastApplication).repository
        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendConfigure()
            sendSlide()
        }
        castContext?.sessionManager!!.addSessionManagerListener(sessionCreatedListener)

        slidePreviewView = findViewById(R.id.tv_setlist_slide_preview)
        songTitleView = findViewById(R.id.tv_current_song_title)

        val setlist: Setlist = intent.getParcelableExtra("setlist")!!
        val setlistWithSongs = runBlocking { repository.getSetlistWithSongs(setlist.id)!! }
        val songs = setlistWithSongs.songs
        var setlistLyricsIndex = 0

        val songLyrics = runBlocking { repository.getSongsWithLyrics(songs) }
            .map { songWithLyricsSections ->
                val songId = songWithLyricsSections.song.id
                val lyricsSectionsText = songWithLyricsSections.lyricsSections
                    .zip(songWithLyricsSections.crossRef)
                    .sortedBy { it.second }
                    .map { it.first.text }
                songId to lyricsSectionsText
            }.toMap()

        setlistLyrics = setlistWithSongs.setlistSongCrossRefs
            .zip(setlistWithSongs.songs)
            .sortedBy { it.first }
            .flatMapIndexed { index: Int, pair: Pair<SetlistSongCrossRef, Song> ->
                val songId = pair.second.songId
                val lyrics = songLyrics[songId]!!

                val indexedTitle = "[$index] ${pair.second.title}"
                songTitles[setlistLyricsIndex] = indexedTitle
                songTitles[setlistLyricsIndex + lyrics.size - 1] = indexedTitle
                songStartPoints[indexedTitle] = setlistLyricsIndex

                setlistLyricsIndex += lyrics.size

                return@flatMapIndexed lyrics
            }


        val songsAndCategories = runBlocking { repository.getSongsAndCategories(songs) }
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

        songItemsAdapter = ControlsSongItemsAdapter(
            this,
            songItemList,
            onItemLongClickListener = onLongClickListener,
            onItemClickListener = onClickListener
        )

        songRecyclerView.adapter = songItemsAdapter
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btn_setlist_blank).setOnClickListener {
            MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
        }

        findViewById<ImageButton>(R.id.btn_setlist_prev).setOnClickListener {
            if (currentLyricsPosition <= 0) {
                return@setOnClickListener
            }
            currentLyricsPosition--
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_prev_song).setOnClickListener {
            val currentSongTitle = songTitles.entries.last {
                it.key <= currentLyricsPosition
            }.value

            val previousSongTitle: String = songTitles.entries.lastOrNull {
                it.key < currentLyricsPosition && it.value != currentSongTitle
            }?.value ?: return@setOnClickListener

            val previousSongStartIndex: Int = songTitles.entries.first {
                it.value == previousSongTitle
            }.key

            currentLyricsPosition = previousSongStartIndex
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_next).setOnClickListener {
            if (currentLyricsPosition >= setlistLyrics.size - 1) {
                return@setOnClickListener
            }
            currentLyricsPosition++
            sendSlide()
        }

        findViewById<ImageButton>(R.id.btn_setlist_next_song).setOnClickListener {
            val currentSongTitle = songTitles.entries.last {
                it.key <= currentLyricsPosition
            }.value

            val nextSongIndex: Int = songTitles.entries.firstOrNull {
                it.key > currentLyricsPosition && it.value != currentSongTitle
            }?.key ?: return@setOnClickListener

            currentLyricsPosition = nextSongIndex
            sendSlide()
        }
    }

    private fun highlightSong(title: String) {
        val songItemPosition: Int = songStartPoints.keys
            .indexOfFirst { songTitle -> songTitle == title }

        songItemsAdapter.songItems.forEachIndexed { index, songItem ->
            songItem.highlight.value = index == songItemPosition
        }

        findViewById<RecyclerView>(R.id.rcv_songs).run {
            scrollToPosition(songItemPosition)
            postInvalidate()
        }
    }

    private fun sendSlide() {
        if (songTitles.containsKey(currentLyricsPosition)) {
            val songTitle = songTitles[currentLyricsPosition]!!
            songTitleView.text = songTitle.replace("^\\[[0-9]+] ".toRegex(), "")
            highlightSong(songTitle)
        }

        slidePreviewView.text = setlistLyrics[currentLyricsPosition]
        MessageHelper.sendContentMessage(
            castContext!!,
            setlistLyrics[currentLyricsPosition]
        )
    }

    private fun selectSong(position: Int): Boolean {
        val title = songItemsAdapter.songItems[position].song.title
        val indexedTitle = "[$position] $title"
        currentLyricsPosition = songStartPoints[indexedTitle]!!
        sendSlide()
        return true
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

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }
}