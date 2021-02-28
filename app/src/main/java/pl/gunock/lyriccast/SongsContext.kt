/*
 * Created by Tomasz Kiljańczyk on 3/1/21 12:09 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/1/21 12:09 AM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.extensions.create
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.extensions.writeText
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SongsContext {
    private const val TAG = "SongsContext"

    var songsDirectory: String = ""
    private var songMap: SortedMap<String, SongMetadataModel> = sortedMapOf()

    var categories: Set<String> = setOf()
        private set

    fun loadSongsMetadata() {
        val loadedSongsMetadata: MutableList<SongMetadataModel> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }

        val fileList = File(songsDirectory).listFiles(fileFilter) ?: return

        val newCategories: SortedSet<String> = sortedSetOf()
        for (file in fileList) {
            Log.v(TAG, "Reading file : ${file.name}")
            val fileText = file.readText(Charsets.UTF_8)
            Log.v(TAG, "File content : ${fileText.replace("\n", "")}")
            val json = JSONObject(fileText)
            Log.v(TAG, "Parsed JSON : $json")

            val songMetadata = SongMetadataModel(json)
            loadedSongsMetadata.add(songMetadata)

            if (!songMetadata.category.isNullOrBlank()) {
                newCategories.add(songMetadata.category)
            }
        }
        Log.v(TAG, "Parsed metadata files: $loadedSongsMetadata")

        categories = newCategories
        fillSongsList(loadedSongsMetadata)
    }

    fun replaceSong(oldSongTitle: String, song: SongMetadataModel, songLyrics: SongLyricsModel) {
        val songTitleNormalized = oldSongTitle.normalize()

        File("$songsDirectory$songTitleNormalized.json").delete()
        File("$songsDirectory$songTitleNormalized.metadata.json").delete()
        songMap.remove(oldSongTitle)

        addSong(song, songLyrics)
        SetlistsContext.replaceSong(oldSongTitle, song.title)
    }

    fun deleteSongs(songTitles: Collection<String>) {
        for (songTitle in songTitles) {
            val songTitleNormalized = songTitle.normalize()

            File("$songsDirectory$songTitleNormalized.json").delete()
            File("$songsDirectory$songTitleNormalized.metadata.json").delete()
            songMap.remove(songTitle)
        }
        SetlistsContext.removeSongs(songTitles)
    }

    fun addSong(song: SongMetadataModel, songLyrics: SongLyricsModel) {
        val songNormalizedTitle = song.title.normalize()
        val songFilePath = "$songsDirectory$songNormalizedTitle"

        Log.d(TAG, "Saving song")
        Log.d(TAG, song.toJSON().toString())
        File("$songFilePath.metadata.json").create()
            .writeText(song.toJSON())

        Log.d(TAG, "Saving lyrics")
        Log.d(TAG, songLyrics.toJSON().toString())
        File("$songFilePath.json").create()
            .writeText(songLyrics.toJSON())

        songMap[song.title] = song
    }

    fun getSongMap(): Map<String, SongMetadataModel> {
        return songMap.toMap()
    }

    fun getSongItems(): Set<SongItemModel> {
        return songMap.map { songMapEntry ->
            SongItemModel(songMapEntry.value)
        }.toSet()
    }

    fun getSongLyrics(title: String): SongLyricsModel? {
        return songMap[title]?.loadLyrics(songsDirectory)
    }

    fun getSongMetadata(title: String): SongMetadataModel? {
        return songMap[title]
    }

    private fun fillSongsList(songs: List<SongMetadataModel>) {
        for (song in songs) {
            songMap[song.title] = song
        }
    }
}