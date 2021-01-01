/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 2:06 PM
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
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.utils.ControlAction
import pl.gunock.lyriccast.utils.MessageHelper

class SetlistControlsActivity : AppCompatActivity() {
    private val tag = "SetlistControlsActivity"

    private var castContext: CastContext? = null
    private lateinit var slidePreview: TextView
    private lateinit var songTitle: TextView
    private lateinit var songListAdapter: SongListAdapter
    private var sessionCreatedListener: SessionCreatedListener? = null

    var currentSlide: Pair<String, String> = Pair("", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_controls)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendSlide()
        }
        castContext!!.sessionManager!!.addSessionManagerListener(sessionCreatedListener)

        slidePreview = findViewById(R.id.text_view_slide_preview2)
        songTitle = findViewById(R.id.current_song_title)

        findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)

            val songTitles = SetlistsContext.currentSetlist!!.songTitles
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

        // Gets first slide
        SetlistsContext.nextSlide()!!
        currentSlide = SetlistsContext.previousSlide()!!
        songTitle.text = currentSlide.first
        sendSlide()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        CastButtonFactory.setUpMediaRouteButton(
//            applicationContext,
//            menu,
//            R.id.menu_cast
//        )
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.menu_import_songs -> importFiles()
//            R.id.menu_settings -> goToSettings()
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

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
    }


    private fun setUpListeners() {
        findViewById<Button>(R.id.button_control_blank2).setOnClickListener {
            MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
        }

        findViewById<Button>(R.id.button_setlist_prev).setOnClickListener {
            var previousSlide = SetlistsContext.previousSlide() ?: return@setOnClickListener

            if (previousSlide == currentSlide) {
                previousSlide = SetlistsContext.previousSlide()!!
            }

            if (previousSlide.first != currentSlide.first) {
                previousSlide = SetlistsContext.previousSlide()!!
                songTitle.text = previousSlide.first
                highlightCurrentSong()
            }

            currentSlide = previousSlide
            sendSlide()
        }

        findViewById<Button>(R.id.button_setlist_next).setOnClickListener {
            var nextSlide = SetlistsContext.nextSlide() ?: return@setOnClickListener

            if (nextSlide == currentSlide) {
                nextSlide = SetlistsContext.nextSlide()!!
            }

            if (nextSlide.first != currentSlide.first) {
                songTitle.text = nextSlide.first
                highlightCurrentSong()
            }

            currentSlide = nextSlide
            sendSlide()
        }
    }

    private fun highlightCurrentSong() {
        songListAdapter.songs.forEach { it.highlight = it.title == songTitle.text }
        songListAdapter.notifyDataSetChanged()

        val songItemPosition = songListAdapter.songs.indexOfFirst { it.title == songTitle.text }
        findViewById<RecyclerView>(R.id.recycler_view_songs).scrollToPosition(songItemPosition)
    }

    private fun sendSlide() {
        slidePreview.text = currentSlide.second
        MessageHelper.sendContentMessage(
            castContext!!,
            currentSlide.second
        )
    }

}