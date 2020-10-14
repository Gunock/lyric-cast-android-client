/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 11:45 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.models.SetlistModel
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SongsContext {
    private const val tag = "SongsContext"

    var songsDirectory = ""
    var songsList: MutableList<SongMetadataModel> = mutableListOf()
    var songsListAdapter: SongListAdapter? = null
    var setlistList: MutableList<SetlistModel> = mutableListOf()
    var setlistListAdapter: SetlistListAdapter? = null
    var categories: MutableSet<String> = mutableSetOf()

    private var presentationIndex: Int = 0
    private var currentSongMetadata: SongMetadataModel? = null
    private var currentSongLyrics: SongLyricsModel? = null

    private val songsMap: MutableMap<String, SongMetadataModel> = mutableMapOf()

    fun loadSongsMetadata() {
        val loadedSongsMeta: MutableList<SongMetadataModel> = mutableListOf()
        val metadataFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }
        categories.clear()
        categories.add("All")
        val fileList = File(songsDirectory).listFiles(metadataFilter)

        fileList!!.forEach { file ->
            Log.d(tag, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val songMetadata = SongMetadataModel(json)
            loadedSongsMeta.add(songMetadata)
            categories.add(songMetadata.category)
        }
        Log.d(tag, "Parsed metadata files: $loadedSongsMeta")

        fillSongsList(loadedSongsMeta)
    }

    fun pickSong(position: Int) {
        currentSongMetadata = songsListAdapter!!.songs[position]
        currentSongLyrics = currentSongMetadata!!.loadLyrics(songsDirectory)
        presentationIndex = 0
    }

    fun nextSlide() {
        if (++presentationIndex >= currentSongMetadata!!.presentation.size) {
            presentationIndex = currentSongMetadata!!.presentation.size - 1
        }
    }

    fun previousSlide() {
        if (--presentationIndex < 0) {
            presentationIndex = 0
        }
    }

    fun getCurrentSlide(): String {
        val slideTag: String = currentSongMetadata!!.presentation[presentationIndex]
        return currentSongLyrics!!.lyrics[slideTag] ?: error("Lyrics not found")
    }

    fun filterSongs(title: String, category: String = "All") {
        songsListAdapter!!.songs = songsList.filter { song ->
            song.title.toLowerCase(Locale.ROOT).contains(title.toLowerCase(Locale.ROOT))
                    && (category == "All" || song.category == category)
        }.toMutableList()
        songsListAdapter!!.notifyDataSetChanged()
    }

    private fun fillSongsList(songs: MutableList<SongMetadataModel>): Void? {
        songsMap.clear()
        songsList = songs
        songs.forEach { song ->
            songsMap[song.title] = song
        }
        songsListAdapter!!.songs = songs
        songsListAdapter!!.notifyDataSetChanged()
        return null
    }

}