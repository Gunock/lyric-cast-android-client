/*
 * Created by Tomasz Kiljańczyk on 2/27/21 2:30 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/26/21 10:51 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
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
import pl.gunock.lyriccast.listeners.SessionCreatedListener
import pl.gunock.lyriccast.models.SetlistModel
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.utils.ControlAction
import pl.gunock.lyriccast.utils.MessageHelper

class SetlistControlsActivity : AppCompatActivity() {
    private companion object {
        private const val TAG = "SetlistControlsActivity"
    }

    private lateinit var slidePreviewView: TextView
    private lateinit var songTitleView: TextView

    private var castContext: CastContext? = null
    private var sessionCreatedListener: SessionCreatedListener? = null
    private lateinit var songListAdapter: SongListAdapter

    private lateinit var setlistLyrics: List<String>
    private var songTitles: MutableMap<Int, String> = mutableMapOf()
    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var currentLyricsIndex: Int = 0

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
        castContext!!.sessionManager!!.addSessionManagerListener(sessionCreatedListener)

        slidePreviewView = findViewById(R.id.text_view_slide_preview2)
        songTitleView = findViewById(R.id.current_song_title)

        val songsMetadata = SongsContext.songMap
        var setlistLyricsIndex = 0
        setlistLyrics = setlist.songTitles.flatMap { songTitle ->
            val songLyrics = SongsContext.getSongLyrics(songTitle).lyrics
            val lyrics = songsMetadata[songTitle]!!.presentation.map { songLyrics[it]!! }

            songTitles[setlistLyricsIndex] = songTitle
            songTitles[setlistLyricsIndex + lyrics.size - 1] = songTitle
            songStartPoints[songTitle] = setlistLyricsIndex

            setlistLyricsIndex += lyrics.size

            lyrics
        }

        findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
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

            songListAdapter = SongListAdapter(songItemList, showRowNumber = true)
            adapter = songListAdapter
        }

        setUpListeners()
    }

    override fun onResume() {
        super.onResume()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val fontSize = prefs.getString("fontSize", "40")
        val backgroundColor = prefs.getString("backgroundColor", "Black")
        val fontColor = prefs.getString("fontColor", "White")
        val configurationJson = JSONObject()
        configurationJson.run {
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

    private fun setUpListeners() {
        findViewById<Button>(R.id.button_control_blank2).setOnClickListener {
            MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
        }

        findViewById<Button>(R.id.button_setlist_prev).setOnClickListener {
            if (currentLyricsIndex <= 0) {
                return@setOnClickListener
            }
            currentLyricsIndex--
            sendSlide()
        }

        findViewById<Button>(R.id.button_setlist_next).setOnClickListener {
            if (currentLyricsIndex >= setlistLyrics.size - 1) {
                return@setOnClickListener
            }
            currentLyricsIndex++
            sendSlide()
        }
    }

    // TODO: Find a workaround for programmatic item highlight
//    private fun highlightSong(title: String) {
//        val songItemPosition: Int = songListAdapter.songs.indexOfFirst { it.title == title }
//        songListAdapter.songs.forEach { it.highlight = it.title == title }
//
//        findViewById<RecyclerView>(R.id.recycler_view_songs).run {
//            scrollToPosition(songItemPosition)
//            postInvalidate()
//        }
//
//    }

    private fun sendSlide() {
        if (songTitles.containsKey(currentLyricsIndex)) {
            val songTitle = songTitles[currentLyricsIndex]!!
            songTitleView.text = songTitle
//            highlightSong(songTitle)
        }

        slidePreviewView.text = setlistLyrics[currentLyricsIndex]
        MessageHelper.sendContentMessage(
            castContext!!,
            setlistLyrics[currentLyricsIndex]
        )
    }

}