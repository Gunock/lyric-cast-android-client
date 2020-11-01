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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.models.SongItemModel

class SetlistControlsActivity : AppCompatActivity() {
    private val tag = "SetlistControlsActivity"

    private var slidePreview: TextView? = null
    private var songTitle: TextView? = null
    private var songListAdapter: SongListAdapter? = null
//    private var sessionCreatedListener: SessionCreatedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setlist_controls)

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

        songTitle!!.text = SetlistsContext.getCurrentSongTitle()
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

    private fun setUpListeners() {
//        findViewById<Button>(R.id.button_control_blank).setOnClickListener {
//            MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
//        }

        findViewById<Button>(R.id.button_setlist_prev).setOnClickListener {
            if (SetlistsContext.previousSlide()) {
                songTitle!!.text = SetlistsContext.getCurrentSongTitle()
                highlightCurrentSong()
            }
            sendSlide()
        }

        findViewById<Button>(R.id.button_setlist_next).setOnClickListener {
            if (SetlistsContext.nextSlide()) {
                songTitle!!.text = SetlistsContext.getCurrentSongTitle()
                highlightCurrentSong()
            }
            sendSlide()
        }
    }

    private fun highlightCurrentSong() {
        val iterator = songListAdapter!!.songs.listIterator()
        while (iterator.hasNext()) {
            val oldValue = iterator.next()
            oldValue.highlight = oldValue.title == SetlistsContext.getCurrentSongTitle()
            iterator.set(oldValue)
        }
        songListAdapter!!.notifyDataSetChanged()
    }

    private fun sendSlide() {
        slidePreview!!.text = SetlistsContext.getCurrentSlide()
//        MessageHelper.sendContentMessage(
//            castContext!!,
//            SongsContext.getCurrentSlide()
//        )
    }

}