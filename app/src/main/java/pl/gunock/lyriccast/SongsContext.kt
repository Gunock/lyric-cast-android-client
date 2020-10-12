/*
 * Created by Tomasz Kiljańczyk on 10/12/20 10:37 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/12/20 9:01 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter

object SongsContext {
    private const val tag = "SongsContext"

    var songsDirectory = ""

    var songsList: MutableList<SongMetadataModel> = mutableListOf()
    var songsListAdapter: SongListAdapter? = null

    private var presentationIndex: Int = 0
    private var currentSongMetadata: SongMetadataModel? = null
    private var currentSongLyrics: SongLyricsModel? = null

    private val songsMap: MutableMap<String, SongMetadataModel> = mutableMapOf()

    fun loadSongsMetadata() {
        val loadedSongsMeta: MutableList<SongMetadataModel> = mutableListOf()
        val metadataFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }
        File(songsDirectory).listFiles(metadataFilter)!!.forEach { file ->
            Log.d(tag, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            loadedSongsMeta.add(SongMetadataModel(json))
        }
        Log.d(tag, "Parsed metadata files: $loadedSongsMeta")

        fillSongsList(loadedSongsMeta)
    }

    fun pickSong(position: Int) {
        currentSongMetadata = songsList[position]
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

    fun filterSongs(text: String) {
        songsListAdapter!!.songs = songsList.filter { song -> song.title.contains(text) }
            .toMutableList()
        songsListAdapter!!.notifyDataSetChanged()
    }

    private fun fillSongsList(songs: MutableList<SongMetadataModel>): Void? {
        songsMap.clear()
        songsList = songs
//        songsListAdapter!!.clear()
        songs.forEach { song ->
            songsMap[song.title] = song
//            songsListAdapter!!.add(song.title)
        }
        songsListAdapter!!.songs = songs
        songsListAdapter!!.notifyDataSetChanged()
        return null
    }

}