/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:07 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SongItemsAdapter
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
    private lateinit var songItemsAdapter: SongItemsAdapter

    private lateinit var setlistLyrics: List<String>
    private var songTitles: MutableMap<Int, String> = mutableMapOf()
    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var currentLyricsPosition: Int = 0

    private lateinit var setlist: Setlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_controls)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val setlistName = intent.getStringExtra("setlistName")!!
        setlist = SetlistsContext.getSetlist(setlistName)

        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendSlide()
        }
        castContext?.sessionManager!!.addSessionManagerListener(sessionCreatedListener)

        slidePreviewView = findViewById(R.id.text_view_slide_preview2)
        songTitleView = findViewById(R.id.current_song_title)

        val songsMetadata = SongsContext.getSongMap()
        var setlistLyricsIndex = 0
        setlistLyrics = setlist.songTitles.flatMap { songTitle ->
            val songLyrics = SongsContext.getSongLyrics(songTitle)!!.lyrics
            val lyrics = songsMetadata[songTitle]!!.presentation
                .map { sectionName -> songLyrics[sectionName]!! }

            songTitles[setlistLyricsIndex] = songTitle
            songTitles[setlistLyricsIndex + lyrics.size - 1] = songTitle
            songStartPoints[songTitle] = setlistLyricsIndex

            setlistLyricsIndex += lyrics.size

            return@flatMap lyrics
        }

        setupRecyclerView()

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val fontSize = prefs.getString("fontSize", "40")
        val backgroundColor = prefs.getString("backgroundColor", "Black")
        val fontColor = prefs.getString("fontColor", "White")
        val configurationJson = JSONObject()

        with(configurationJson) {
            put("fontSize", "${fontSize}px")
            put("backgroundColor", backgroundColor)
            put("fontColor", fontColor)
        }

        MessageHelper.sendControlMessage(
            castContext!!,
            ControlAction.CONFIGURE,
            configurationJson
        )
        sendSlide()
    }

    private fun setupRecyclerView() {
        val songTitles: List<String> = setlist.songTitles
        val songItemList: List<SongItem> = SongsContext.getSongItems()
            .filter { songItem -> songTitles.contains(songItem.title) }

        with(findViewById<RecyclerView>(R.id.recycler_view_songs)) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)

            val onLongClickListener =
                LongClickAdapterItemListener { _: SongItemsAdapter.SongViewHolder, position: Int, _ ->
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(position)
                }

            val onClickListener =
                ClickAdapterItemListener { _: SongItemsAdapter.SongViewHolder, position: Int, _ ->
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(position)
                }

            songItemsAdapter = SongItemsAdapter(
                songItemList.toMutableList(),
                showRowNumber = true,
                onItemClickListener = onClickListener,
                onItemLongClickListener = onLongClickListener
            )

            adapter = songItemsAdapter
        }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.button_control_blank2).setOnClickListener {
            MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
        }

        findViewById<Button>(R.id.button_setlist_prev).setOnClickListener {
            if (currentLyricsPosition <= 0) {
                return@setOnClickListener
            }
            currentLyricsPosition--
            sendSlide()
        }

        findViewById<Button>(R.id.button_setlist_next).setOnClickListener {
            if (currentLyricsPosition >= setlistLyrics.size - 1) {
                return@setOnClickListener
            }
            currentLyricsPosition++
            sendSlide()
        }
    }

    private fun highlightSong(title: String) {
        val songItemPosition: Int = songItemsAdapter.songItems
            .indexOfFirst { songItem -> songItem.title == title }
        songItemsAdapter.songItems.forEach { songItem ->
            songItem.highlight = songItem.title == title
        }
        songItemsAdapter.notifyDataSetChanged()

        findViewById<RecyclerView>(R.id.recycler_view_songs).run {
            scrollToPosition(songItemPosition)
            postInvalidate()
        }
    }

    private fun sendSlide() {
        if (songTitles.containsKey(currentLyricsPosition)) {
            val songTitle = songTitles[currentLyricsPosition]!!
            songTitleView.text = songTitle
            highlightSong(songTitle)
        }

        slidePreviewView.text = setlistLyrics[currentLyricsPosition]
        MessageHelper.sendContentMessage(
            castContext!!,
            setlistLyrics[currentLyricsPosition]
        )
    }

    private fun selectSong(position: Int): Boolean {
        val item: SongItem = songItemsAdapter.songItems[position]
        currentLyricsPosition = songStartPoints[item.title]!!
        sendSlide()
        return true
    }
}