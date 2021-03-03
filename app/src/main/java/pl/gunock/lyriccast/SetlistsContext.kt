/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:07 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.models.Setlist
import pl.gunock.lyriccast.models.SetlistItem
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SetlistsContext {
    private const val TAG = "SetlistsContext"

    var setlistsDirectory: String = ""

    private var setlists: SortedSet<Setlist> = sortedSetOf()

    fun loadSetlists() {
        val loadedSetlists: SortedSet<Setlist> = sortedSetOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(setlistsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return
        }

        for (file in fileList) {
            Log.d(TAG, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val setlist = Setlist(json)
            loadedSetlists.add(setlist)
        }
        Log.d(TAG, "Parsed setlist files: $loadedSetlists")

        setlists = loadedSetlists
    }

    fun saveSetlist(setlist: Setlist) {
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
        val modifiedSetlists: MutableList<Setlist> = mutableListOf()
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
        val modifiedSetlists: MutableList<Setlist> = mutableListOf()
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

    fun getSetlist(setlistName: String): Setlist {
        return setlists.first { setlist -> setlist.name == setlistName }
    }

    fun getSetlistItems(): Set<SetlistItem> {
        return setlists.map { setlist -> SetlistItem(setlist) }.toSet()
    }

}