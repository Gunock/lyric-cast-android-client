/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 2:06 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SongsContext {
    private const val tag = "SongsContext"

    var songsDirectory: String = ""
    var songMap: MutableMap<String, SongMetadataModel> = mutableMapOf()

    // TODO: Try to move adapters to activities/fragments
    val songItemList: MutableList<SongItemModel> = mutableListOf()
    var songsListAdapter: SongListAdapter? = null

    var categories: MutableSet<String> = mutableSetOf()

    private var presentationIterator: ListIterator<String>? = null
    private var presentationCurrentTag: String = ""
    private var currentSongMetadata: SongMetadataModel? = null
    private var currentSongLyrics: SongLyricsModel? = null

    fun loadSongsMetadata(): List<SongMetadataModel> {
        val loadedSongsMetadata: MutableList<SongMetadataModel> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }
        categories.clear()
        categories.add("All")
        val fileList = File(songsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return listOf()
        }

        for (file in fileList) {
            Log.d(tag, "Reading file : ${file.name}")
            val fileText = file.readText(Charsets.UTF_8)
            Log.d(tag, "File content : ${fileText.replace("\n", "")}")
            val json = JSONObject(fileText)
            Log.d(tag, "Parsed JSON : $json")

            val songMetadata = SongMetadataModel(json)
            loadedSongsMetadata.add(songMetadata)
            categories.add(songMetadata.category)
        }
        Log.d(tag, "Parsed metadata files: $loadedSongsMetadata")

        return loadedSongsMetadata
    }

    fun setupSongListAdapter(showCheckBox: Boolean = false) {
        Log.d(tag, "setupSongListAdapter invoked")
        songItemList.clear()
        for (i in songMap.values.indices) {
            songItemList.add(SongItemModel(songMap.values.elementAt(i)))
        }
        songsListAdapter = SongListAdapter(songItemList, showCheckBox)
    }

    fun filterSongs(title: String, category: String = "All") {
        Log.d(tag, "filterSongs invoked")
        songsListAdapter!!.songs = songItemList.filter { song ->
            song.title.toLowerCase(Locale.ROOT).contains(title.toLowerCase(Locale.ROOT))
                    && (category == "All" || song.category == category)
        }.toMutableList()
        songsListAdapter!!.notifyDataSetChanged()
    }

    fun fillSongsList(songs: List<SongMetadataModel>) {
        songItemList.clear()
        for (song in songs) {
            addSong(song)
        }
        songsListAdapter!!.songs = songItemList
        songsListAdapter!!.notifyDataSetChanged()
    }

    fun addSong(song: SongMetadataModel) {
        songMap[song.title] = song
        songItemList.add(SongItemModel(song))
        songsListAdapter?.notifyDataSetChanged()
    }

    fun pickSong(title: String) {
        currentSongMetadata = songMap[title]
        currentSongLyrics = currentSongMetadata!!.loadLyrics(songsDirectory)
        presentationIterator = currentSongMetadata!!.presentation.listIterator()
        presentationCurrentTag = presentationIterator!!.next()
    }

    fun nextSlide(): Boolean {
        if (presentationIterator!!.hasNext()) {
            presentationCurrentTag = presentationIterator!!.next()
            return true
        }
        return false
    }

    fun previousSlide(): Boolean {
        if (presentationIterator!!.hasPrevious()) {
            presentationCurrentTag = presentationIterator!!.previous()
            return true
        }
        return false
    }

    fun getCurrentSlide(): String {
        return currentSongLyrics!!.lyrics[presentationCurrentTag] ?: error("Lyrics not found")
    }
}