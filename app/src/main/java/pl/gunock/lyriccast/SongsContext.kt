/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:42 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 4:19 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SongsContext {
    private const val TAG = "SongsContext"

    var songsDirectory: String = ""
    var songMap: SortedMap<String, SongMetadataModel> = sortedMapOf()

    val songItemList: MutableList<SongItemModel> = mutableListOf()

    var categories: MutableSet<String> = mutableSetOf()

    fun loadSongsMetadata() {
        val loadedSongsMetadata: MutableList<SongMetadataModel> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }
        categories.clear()
        categories.add("All")
        val fileList = File(songsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            throw RuntimeException("Could not load songs metadata.")
        }

        for (file in fileList) {
            Log.d(TAG, "Reading file : ${file.name}")
            val fileText = file.readText(Charsets.UTF_8)
            Log.d(TAG, "File content : ${fileText.replace("\n", "")}")
            val json = JSONObject(fileText)
            Log.d(TAG, "Parsed JSON : $json")

            val songMetadata = SongMetadataModel(json)
            loadedSongsMetadata.add(songMetadata)
            categories.add(songMetadata.category)
        }
        Log.d(TAG, "Parsed metadata files: $loadedSongsMetadata")

        fillSongsList(loadedSongsMetadata)
    }

    fun deleteSongs(songTitles: List<String>) {
        for (songTitle in songTitles) {
            val songTitleNormalized = songTitle.normalize()

            File("$songsDirectory$songTitleNormalized.json").delete()
            File("$songsDirectory$songTitleNormalized.metadata.json").delete()
            songMap.remove(songTitle)
        }
    }

    private fun fillSongsList(songs: List<SongMetadataModel>) {
        songItemList.clear()
        for (song in songs) {
            addSong(song)
        }
    }

    fun addSong(song: SongMetadataModel) {
        songMap[song.title] = song
    }

    fun getSongLyrics(title: String): SongLyricsModel? {
        return songMap[title]?.loadLyrics(songsDirectory)
    }

    fun getSongMetadata(title: String): SongMetadataModel? {
        return songMap[title]
    }
}