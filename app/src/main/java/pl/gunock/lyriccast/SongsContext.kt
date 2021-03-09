/*
 * Created by Tomasz Kiljańczyk on 3/9/21 2:21 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 1:59 AM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.extensions.create
import pl.gunock.lyriccast.extensions.writeText
import pl.gunock.lyriccast.models.SongItem
import pl.gunock.lyriccast.models.SongLyrics
import pl.gunock.lyriccast.models.SongMetadata
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SongsContext {
    private const val TAG = "SongsContext"

    var songsDirectory: String = ""
    private var songMap: SortedMap<Long, SongMetadata> = sortedMapOf()

    fun loadSongsMetadata() {
        val loadedSongsMetadata: MutableList<SongMetadata> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }

        val fileList = File(songsDirectory).listFiles(fileFilter) ?: return

        for (file in fileList) {
            Log.v(TAG, "Reading file : ${file.name}")
            val fileText = file.readText(Charsets.UTF_8)
            Log.v(TAG, "File content : ${fileText.replace("\n", "")}")
            val json = JSONObject(fileText)
            Log.v(TAG, "Parsed JSON : $json")

            val songMetadata = SongMetadata(json)
            loadedSongsMetadata.add(songMetadata)
        }
        Log.v(TAG, "Parsed metadata files: $loadedSongsMetadata")

        fillSongsList(loadedSongsMetadata)
    }

    fun containsSong(title: String): Boolean {
        return songMap.values.firstOrNull { song -> song.title == title } != null
    }

    fun deleteSongs(songIds: Collection<Long>) {
        for (songId in songIds) {
            File("$songsDirectory$songId.json").delete()
            File("$songsDirectory$songId.metadata.json").delete()
            songMap.remove(songId)
        }
        SetlistsContext.removeSongs(songIds)
    }

    fun saveSong(
        title: String,
        categoryId: Long,
        presentation: List<String>,
        songLyrics: SongLyrics,
        id: Long = System.currentTimeMillis()
    ) {
        val song = SongMetadata(id)
        song.title = title
        song.categoryId = categoryId
        song.presentation = presentation

        val songFilePath = "$songsDirectory${id}"

        Log.d(TAG, "Saving song")
        Log.d(TAG, song.toJson().toString())
        File("$songFilePath.metadata.json").create()
            .writeText(song.toJson())

        Log.d(TAG, "Saving lyrics")
        Log.d(TAG, songLyrics.toJson().toString())
        File("$songFilePath.json").create()
            .writeText(songLyrics.toJson())

        songMap[song.id] = song
    }

    fun getSongMap(): Map<Long, SongMetadata> {
        return songMap.toMap()
    }

    fun getSongItems(): Set<SongItem> {
        return songMap.map { songMapEntry ->
            SongItem(songMapEntry.value)
        }.toSet()
    }

    fun getSongTitle(id: Long): String {
        return getSongMetadata(id)!!.title
    }

    fun getSongLyrics(id: Long): SongLyrics? {
        return songMap[id]?.loadLyrics(songsDirectory)
    }

    fun getSongMetadata(id: Long): SongMetadata? {
        return songMap[id]
    }

    fun removeCategories(ids: Collection<Long>) {
        val modifiedSongIds: MutableList<Long> = mutableListOf()
        songMap.forEach { mapEntry ->
            if (ids.contains(mapEntry.value.categoryId)) {
                mapEntry.value.categoryId = Long.MIN_VALUE
                modifiedSongIds.add(mapEntry.value.id)
            }
        }

        for (id in modifiedSongIds) {
            val song = songMap[id]!!
            saveSong(song)
        }
    }

    private fun fillSongsList(songs: List<SongMetadata>) {
        for (song in songs) {
            songMap[song.id] = song
        }
    }

    private fun saveSong(song: SongMetadata) {
        val songFilePath = "$songsDirectory${song.id}"

        Log.d(TAG, "Saving song")
        Log.d(TAG, song.toJson().toString())
        File("$songFilePath.metadata.json").create()
            .writeText(song.toJson())
    }
}