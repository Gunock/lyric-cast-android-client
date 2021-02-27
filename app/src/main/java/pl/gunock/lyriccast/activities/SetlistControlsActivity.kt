/*
 * Created by Tomasz Kiljańczyk on 2/27/21 5:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 5:07 PM
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
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.adapters.listeners.ClickAdapterListener
import pl.gunock.lyriccast.adapters.listeners.LongClickAdapterListener
import pl.gunock.lyriccast.listeners.SessionCreatedListener
import pl.gunock.lyriccast.models.SetlistModel
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.utils.ControlAction
import pl.gunock.lyriccast.utils.MessageHelper

class SetlistControlsActivity : AppCompatActivity() {
    private lateinit var slidePreviewView: TextView
    private lateinit var songTitleView: TextView

    private var castContext: CastContext? = null
    private var sessionCreatedListener: SessionCreatedListener? = null
    private lateinit var songListAdapter: SongListAdapter

    private lateinit var setlistLyrics: List<String>
    private var songTitles: MutableMap<Int, String> = mutableMapOf()
    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var currentLyricsPosition: Int = 0

    private lateinit var setlist: SetlistModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_controls)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val setlistName = intent.getStringExtra("setlistName")!!
        setlist = SetlistsContext.getSetlist(setlistName)!!

        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendSlide()
        }
        castContext?.sessionManager!!.addSessionManagerListener(sessionCreatedListener)

        slidePreviewView = findViewById(R.id.text_view_slide_preview2)
        songTitleView = findViewById(R.id.current_song_title)

        val songsMetadata = SongsContext.songMap
        var setlistLyricsIndex = 0
        setlistLyrics = setlist.songTitles.flatMap { songTitle ->
            val songLyrics = SongsContext.getSongLyrics(songTitle)!!.lyrics
            val lyrics = songsMetadata[songTitle]!!.presentation
                .map { sectionName -> songLyrics[sectionName]!! }

            songTitles[setlistLyricsIndex] = songTitle
            songTitles[setlistLyricsIndex + lyrics.size - 1] = songTitle
            songStartPoints[songTitle] = setlistLyricsIndex

            setlistLyricsIndex += lyrics.size

            lyrics
        }

        with(findViewById<RecyclerView>(R.id.recycler_view_songs)) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)

            val songTitles = setlist.songTitles
            val songItemList: MutableList<SongItemModel> = mutableListOf()
            for (i in songTitles.indices) {
                songItemList.add(SongItemModel(SongsContext.songMap.getValue(songTitles[i])))
            }

            val iterator = songItemList.listIterator()
            while (iterator.hasNext()) {
                val oldValue = iterator.next()
                if (oldValue.title == songTitles[0]) {
                    oldValue.highlight = true
                    iterator.set(oldValue)
                }
            }

            val onLongClickListener =
                LongClickAdapterListener { _: SongListAdapter.SongViewHolder, position: Int, _ ->
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(position)
                }

            val onClickListener =
                ClickAdapterListener { _: SongListAdapter.SongViewHolder, position: Int, _ ->
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(position)
                }

            songListAdapter = SongListAdapter(
                songItemList,
                showRowNumber = true,
                onClickListener = onClickListener,
                onLongClickListener = onLongClickListener
            )
            adapter = songListAdapter
        }

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
        val songItemPosition: Int = songListAdapter.songItems
            .indexOfFirst { songItem -> songItem.title == title }
        songListAdapter.songItems.forEach { songItem ->
            songItem.highlight = songItem.title == title
        }
        songListAdapter.notifyDataSetChanged()

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
        val item: SongItemModel = songListAdapter.songItems[position]
        currentLyricsPosition = songStartPoints[item.title]!!
        sendSlide()
        return true
    }
}