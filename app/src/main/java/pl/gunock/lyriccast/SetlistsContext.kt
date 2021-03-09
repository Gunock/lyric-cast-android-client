/*
 * Created by Tomasz Kiljańczyk on 3/9/21 1:07 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 12:30 AM
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

    fun saveSetlist(
        setlistName: String,
        songIds: List<Long>,
        id: Long = System.currentTimeMillis()
    ) {
        val setlist = Setlist(id, setlistName, songIds)

        val json = setlist.toJson()
        val setlistFile = File("$setlistsDirectory/${setlist.id}.json")
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

    fun removeSongs(songIds: Collection<Long>) {
        val modifiedSetlists: MutableList<Setlist> = mutableListOf()
        setlists.forEach { setlist ->
            val modifiedSongTitles = setlist.songIds.filter { songId ->
                !songIds.contains(songId)
            }

            if (setlist.songIds != modifiedSongTitles) {
                modifiedSetlists.add(setlist)
            }

            setlist.songIds = modifiedSongTitles
        }

        for (setlist in modifiedSetlists) {
            if (setlist.songIds.isEmpty()) {
                deleteSetlists(listOf(setlist.name))
            } else {
                saveSetlist(setlist.name, setlist.songIds, setlist.id)
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