/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/19/20 12:00 AM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.models.SetlistModel
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SongsContext {
    private const val tag = "SongsContext"

    var songsDirectory: String = ""
    var setlistsDirectory: String = ""
    var songList: MutableList<SongMetadataModel> = mutableListOf()
    val songItemList: MutableList<SongItemModel> = mutableListOf()
    var songsListAdapter: SongListAdapter? = null

    var setlistList: MutableList<SetlistModel> = mutableListOf()
    var setlistListAdapter: SetlistListAdapter? = null
    var categories: MutableSet<String> = mutableSetOf()

    private var presentationIterator: ListIterator<String>? = null
    private var presentationCurrentTag: String = ""
    private var currentSongMetadata: SongMetadataModel? = null
    private var currentSongLyrics: SongLyricsModel? = null

    fun loadSongsMetadata() {
        val loadedSongsMeta: MutableList<SongMetadataModel> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }
        categories.clear()
        categories.add("All")
        val fileList = File(songsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return
        }

        for (file in fileList) {
            Log.d(tag, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val songMetadata = SongMetadataModel(json)
            loadedSongsMeta.add(songMetadata)
            categories.add(songMetadata.category)
        }
        Log.d(tag, "Parsed metadata files: $loadedSongsMeta")

        fillSongsList(loadedSongsMeta)
    }

    fun loadSetlists() {
        val loadedSetlists: MutableList<SetlistModel> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(setlistsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return
        }

        for (file in fileList) {
            if (file.isDirectory) {
                file.deleteRecursively()
            }

            Log.d(tag, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val songSetlist = SetlistModel(json)
            loadedSetlists.add(songSetlist)
        }
        Log.d(tag, "Parsed metadata files: $loadedSetlists")

        setlistList = loadedSetlists
    }

    fun pickSong(position: Int) {
        currentSongMetadata = songList[songsListAdapter!!.songs[position].originalPosition]
        currentSongLyrics = currentSongMetadata!!.loadLyrics(songsDirectory)
        presentationIterator = currentSongMetadata!!.presentation.listIterator()
        presentationCurrentTag = presentationIterator!!.next()
    }

    fun nextSlide() {
        if (presentationIterator!!.hasNext()) {
            presentationCurrentTag = presentationIterator!!.next()
        }
    }

    fun previousSlide() {
        if (presentationIterator!!.hasPrevious()) {
            presentationCurrentTag = presentationIterator!!.previous()
        }
    }

    fun getCurrentSlide(): String {
        return currentSongLyrics!!.lyrics[presentationCurrentTag] ?: error("Lyrics not found")
    }

    fun saveSetlist(setlist: SetlistModel) {
        val json = setlist.toJson()
        val setlistFile = File("$setlistsDirectory/${setlist.name}.json")
        File(setlistsDirectory).mkdirs()
        setlistFile.writeText(json.toString())
    }

    fun setupSongListAdapter(showCheckBox: Boolean = false) {
        for (i in 0 until songList.size) {
            songItemList.add(SongItemModel(i, songList[i]))
        }
        songsListAdapter = SongListAdapter(songItemList, showCheckBox)
    }

    fun filterSongs(title: String, category: String = "All") {
        songsListAdapter!!.songs = songItemList.filter { song ->
            song.title.toLowerCase(Locale.ROOT).contains(title.toLowerCase(Locale.ROOT))
                    && (category == "All" || song.category == category)
        }.toMutableList()
        songsListAdapter!!.notifyDataSetChanged()
    }

    private fun fillSongsList(songs: MutableList<SongMetadataModel>) {
        songList = songs
        songItemList.clear()
        for (i in 0 until songList.size) {
            songItemList.add(SongItemModel(i, songList[i]))
        }
        songsListAdapter!!.songs = songItemList
        songsListAdapter!!.notifyDataSetChanged()
    }

}