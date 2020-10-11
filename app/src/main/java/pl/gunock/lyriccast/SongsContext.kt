/*
 * Created by Tomasz Kiljańczyk on 10/11/20 11:21 PM
 * Copyright (c) 2020 . All rights reserved.
 *  Last modified 10/11/20 10:18 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File
import java.io.FilenameFilter

object SongsContext {
    private const val tag = "SongsContext"

    var songsDirectory = ""

    var songsList: MutableList<SongMetadataModel> = mutableListOf()
    var songsListAdapter: SongListAdapter? = null

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