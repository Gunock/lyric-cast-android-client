/*
 * Created by Tomasz Kiljańczyk on 3/8/21 11:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 11:18 PM
 */

package pl.gunock.lyriccast

import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.models.SongItem
import pl.gunock.lyriccast.models.SongLyrics
import pl.gunock.lyriccast.models.SongMetadata
import java.io.File
import java.util.*

object SongsContext {
    private const val TAG = "SongsContext"

    var songsDirectory: String = ""
    private var songMap: SortedMap<String, SongMetadata> = sortedMapOf()

    fun loadSongsMetadata() {
//        val loadedSongsMetadata: MutableList<SongMetadata> = mutableListOf()
//        val fileFilter = FilenameFilter { _, name -> name.endsWith(".metadata.json") }
//
//        val fileList = File(songsDirectory).listFiles(fileFilter) ?: return
//
//        for (file in fileList) {
//            Log.v(TAG, "Reading file : ${file.name}")
//            val fileText = file.readText(Charsets.UTF_8)
//            Log.v(TAG, "File content : ${fileText.replace("\n", "")}")
//            val json = JSONObject(fileText)
//            Log.v(TAG, "Parsed JSON : $json")
//
//            val songMetadata = SongMetadata(json)
//            loadedSongsMetadata.add(songMetadata)
//        }
//        Log.v(TAG, "Parsed metadata files: $loadedSongsMetadata")
//
//        fillSongsList(loadedSongsMetadata)
    }

    fun containsSong(title: String): Boolean {
        return songMap.containsKey(title)
    }

    fun replaceSong(oldSongTitle: String, song: SongMetadata, songLyrics: SongLyrics) {
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

    fun addSong(song: SongMetadata, songLyrics: SongLyrics) {
        // TODO: Uncomment before merge
//        val songNormalizedTitle = song.title.normalize()
//        val songFilePath = "$songsDirectory$songNormalizedTitle"
//
//        Log.d(TAG, "Saving song")
//        Log.d(TAG, song.toJSON().toString())
//        File("$songFilePath.metadata.json").create()
//            .writeText(song.toJSON())
//
//        Log.d(TAG, "Saving lyrics")
//        Log.d(TAG, songLyrics.toJSON().toString())
//        File("$songFilePath.json").create()
//            .writeText(songLyrics.toJSON())

        songMap[song.title] = song
    }

    fun getSongMap(): Map<String, SongMetadata> {
        return songMap.toMap()
    }

    fun getSongItems(): Set<SongItem> {
        return songMap.map { songMapEntry ->
            SongItem(songMapEntry.value)
        }.toSet()
    }

    fun getSongLyrics(title: String): SongLyrics? {
        return songMap[title]?.loadLyrics(songsDirectory)
    }

    fun getSongMetadata(title: String): SongMetadata? {
        return songMap[title]
    }

    // TODO: Uncomment before merge
//    private fun fillSongsList(songs: List<SongMetadata>) {
//        for (song in songs) {
//            songMap[song.title] = song
//        }
//    }
}