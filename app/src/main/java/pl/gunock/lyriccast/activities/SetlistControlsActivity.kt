/*
 * Created by Tomasz Kiljańczyk on 3/17/21 12:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/17/21 12:00 AM
 */

package pl.gunock.lyriccast.activities

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.ControlsSongItemsAdapter
import pl.gunock.lyriccast.enums.ControlAction
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.listeners.SessionCreatedListener
import pl.gunock.lyriccast.models.Setlist
import pl.gunock.lyriccast.models.SongItem

class SetlistControlsActivity : AppCompatActivity() {
    private lateinit var slidePreviewView: TextView
    private lateinit var songTitleView: TextView

    private var castContext: CastContext? = null
    private var sessionCreatedListener: SessionCreatedListener? = null
    private lateinit var songItemsAdapter: ControlsSongItemsAdapter

    private lateinit var setlistLyrics: List<String>
    private var songTitles: MutableMap<Int, String> = mutableMapOf()
    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var currentLyricsPosition: Int = 0

    private lateinit var setlist: Setlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_controls)
        setSupportActionBar(findViewById(R.id.toolbar_controls))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val setlistName = intent.getStringExtra("setlistName")!!
        setlist = SetlistsContext.getSetlist(setlistName)

        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendConfigure()
            sendSlide()
        }
        castContext?.sessionManager!!.addSessionManagerListener(sessionCreatedListener)

        slidePreviewView = findViewById(R.id.tv_setlist_slide_preview)
        songTitleView = findViewById(R.id.tv_current_song_title)

        val songsMetadata = SongsContext.getSongMap()
        var setlistLyricsIndex = 0

        setlistLyrics = setlist.songIds.flatMapIndexed { index: Int, songId: Long ->
            val songTitle = SongsContext.getSongTitle(songId)
            val songLyrics = SongsContext.getSongLyrics(songId)!!.lyrics
            val lyrics = songsMetadata[songId]!!.presentation
                .map { sectionName -> songLyrics[sectionName]!! }

            val indexedTitle = "[$index] $songTitle"
            songTitles[setlistLyricsIndex] = indexedTitle
            songTitles[setlistLyricsIndex + lyrics.size - 1] = indexedTitle
            songStartPoints[indexedTitle] = setlistLyricsIndex

            setlistLyricsIndex += lyrics.size

            return@flatMapIndexed lyrics
        }

        setupRecyclerView()

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        sendConfigure()
        sendSlide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_controls, menu)

        CastButtonFactory.setUpMediaRouteButton(
            baseContext,
            menu,
            R.id.menu_cast
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> goToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
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

        val songMap = SongsContext.getSongMap()
        val songItemList: List<SongItem> = setlist.songIds.map { songId ->
            SongItem(songMap[songId]!!)
        }

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

        findViewById<Button>(R.id.btn_setlist_prev).setOnClickListener {
            if (currentLyricsPosition <= 0) {
                return@setOnClickListener
            }
            currentLyricsPosition--
            sendSlide()
        }

        findViewById<Button>(R.id.btn_setlist_next).setOnClickListener {
            if (currentLyricsPosition >= setlistLyrics.size - 1) {
                return@setOnClickListener
            }
            currentLyricsPosition++
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
        val title = songItemsAdapter.songItems[position].title
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

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }
}