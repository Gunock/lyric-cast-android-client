/*
 * Created by Tomasz Kiljańczyk on 3/1/21 12:09 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/28/21 11:44 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.models.SetlistItemModel
import pl.gunock.lyriccast.models.SetlistModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SetlistsContext {
    private const val TAG = "SetlistsContext"

    var setlistsDirectory: String = ""

    private var setlists: SortedSet<SetlistModel> = sortedSetOf()

    fun loadSetlists() {
        val loadedSetlists: SortedSet<SetlistModel> = sortedSetOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(setlistsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return
        }

        for (file in fileList) {
            Log.d(TAG, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val setlist = SetlistModel(json)
            loadedSetlists.add(setlist)
        }
        Log.d(TAG, "Parsed setlist files: $loadedSetlists")

        setlists = loadedSetlists
    }

    fun saveSetlist(setlist: SetlistModel) {
        val json = setlist.toJson()
        val setlistFile = File("$setlistsDirectory/${setlist.name}.json")
        File(setlistsDirectory).mkdirs()
        setlistFile.writeText(json.toString())

        setlists.add(setlist)
    }

    fun deleteSetlists(setlistNames: Collection<String>) {
        for (setlistName in setlistNames) {
            val setlistFile = File("$setlistsDirectory/${setlistName}.json")
            setlistFile.delete()
        }

        setlists = setlists.filter { setlist ->
            !setlistNames.contains(setlist.name)
        }.toSortedSet()
    }

    fun replaceSong(oldSongTitle: String, newSongTitle: String) {
        val modifiedSetlists: MutableList<SetlistModel> = mutableListOf()
        setlists.forEach { setlist ->
            val modifiedSongTitles = setlist.songTitles.map { songTitle ->
                if (songTitle == oldSongTitle) newSongTitle else songTitle
            }

            if (setlist.songTitles != modifiedSetlists) {
                modifiedSetlists.add(setlist)
            }

            setlist.songTitles = modifiedSongTitles
        }

        for (setlist in modifiedSetlists) {
            saveSetlist(setlist)
        }
    }

    fun removeSongs(songTitles: Collection<String>) {
        val modifiedSetlists: MutableList<SetlistModel> = mutableListOf()
        setlists.forEach { setlist ->
            val modifiedSongTitles = setlist.songTitles.filter { songTitle ->
                !songTitles.contains(songTitle)
            }

            if (setlist.songTitles != modifiedSongTitles) {
                modifiedSetlists.add(setlist)
            }

            setlist.songTitles = modifiedSongTitles
        }

        for (setlist in modifiedSetlists) {
            if (setlist.songTitles.isEmpty()) {
                deleteSetlists(listOf(setlist.name))
            } else {
                saveSetlist(setlist)
            }
        }
    }

    fun getSetlist(setlistName: String): SetlistModel {
        return setlists.first { setlist -> setlist.name == setlistName }
    }

    fun getSetlistItems(): Set<SetlistItemModel> {
        return setlists.map { setlist -> SetlistItemModel(setlist) }.toSet()
    }

}