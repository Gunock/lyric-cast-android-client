/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 8:57 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter
import java.util.*
import kotlin.system.measureTimeMillis

object SongsContext {
    private const val tag = "SongsContext"

    var songsDirectory: String = ""
    var songMap: SortedMap<String, SongMetadataModel> = sortedMapOf()

    // TODO: Try to move adapters to activities/fragments
    val songItemList: MutableList<SongItemModel> = mutableListOf()
    var songListAdapter: SongListAdapter? = null

    var categories: MutableSet<String> = mutableSetOf()

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

    fun deleteSongs(songTitles: List<String>) {
        for (songTitle in songTitles) {
            val songTitleNormalized = songTitle.normalize()

            File("$songsDirectory$songTitleNormalized.json").delete()
            File("$songsDirectory$songTitleNormalized.metadata.json").delete()
            songMap.remove(songTitle)
        }
    }

    fun setupSongListAdapter(showCheckBox: Boolean = false, showAuthor: Boolean = true) {
        Log.d(tag, "setupSongListAdapter invoked")
        songItemList.clear()
        for (i in songMap.values.indices) {
            songItemList.add(SongItemModel(songMap.values.elementAt(i)))
        }
        songListAdapter = SongListAdapter(
            songItemList,
            showCheckBox = showCheckBox,
            showAuthor = showAuthor
        )
    }

    fun filterSongs(title: String, category: String = "All", isSelected: Boolean? = null) {
        Log.d(tag, "filterSongs invoked")

        val predicate = if (isSelected == null) { song: SongItemModel ->
            val titleCondition = song.title.toLowerCase(Locale.ROOT)
                .normalize()
                .contains(title.toLowerCase(Locale.ROOT).normalize())
            val categoryCondition = (category == "All" || song.category == category)

            titleCondition && categoryCondition
        } else { song: SongItemModel ->
            if (song.isSelected != isSelected) {
                false
            } else {
                val titleCondition = song.title.toLowerCase(Locale.ROOT)
                    .normalize()
                    .contains(title.toLowerCase(Locale.ROOT).normalize())
                val categoryCondition = (category == "All" || song.category == category)

                titleCondition && categoryCondition
            }
        }

        val duration = measureTimeMillis {
            songListAdapter!!.songs = songItemList.filter(predicate).toMutableList()
        }
        Log.d(tag, "Filtering took : ${duration}ms")

        songListAdapter!!.notifyDataSetChanged()
    }

    fun fillSongsList(songs: List<SongMetadataModel>) {
        songItemList.clear()
        for (song in songs) {
            addSong(song)
        }
        songListAdapter!!.songs = songItemList
        songListAdapter!!.notifyDataSetChanged()
    }

    fun addSong(song: SongMetadataModel) {
        songMap[song.title] = song
        songItemList.add(SongItemModel(song))
        songListAdapter?.notifyDataSetChanged()
    }

    fun getSongLyrics(title: String): SongLyricsModel {
        return songMap[title]!!.loadLyrics(songsDirectory)
    }

    fun getSongMetadata(title: String): SongMetadataModel {
        return songMap[title]!!
    }
}