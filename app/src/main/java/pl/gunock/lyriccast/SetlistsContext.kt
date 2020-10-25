/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/23/20 10:13 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.models.SetlistItemModel
import pl.gunock.lyriccast.models.SetlistModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SetlistsContext {
    private const val tag = "SongsContext"

    var setlistsDirectory: String = ""

    var setlistList: MutableList<SetlistModel> = mutableListOf()
    val setlistItemList: MutableList<SetlistItemModel> = mutableListOf()
    var setlistListAdapter: SetlistListAdapter? = null

    var currentSetlist: SetlistModel? = null
    private var presentationIterator: ListIterator<String>? = null
    private var currentSongTitle: String = ""
    private var presentationGap: Boolean = false

    fun loadSetlists(): List<SetlistModel> {
        val loadedSetlists: MutableList<SetlistModel> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(setlistsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return listOf()
        }

        for (file in fileList) {
            Log.d(tag, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val setlist = SetlistModel(json)
            loadedSetlists.add(setlist)
        }
        Log.d(tag, "Parsed setlist files: $loadedSetlists")

        return loadedSetlists
    }

    fun saveSetlist(setlist: SetlistModel) {
        val json = setlist.toJson()
        val setlistFile = File("$setlistsDirectory/${setlist.name}.json")
        File(setlistsDirectory).mkdirs()
        setlistFile.writeText(json.toString())
    }

    fun setupSetlistListAdapter() {
        setlistItemList.clear()
        for (i in setlistList.indices) {
            setlistItemList.add(SetlistItemModel(i, setlistList[i]))
        }
        setlistListAdapter = SetlistListAdapter(setlistItemList)
    }

    fun pickSetlist(position: Int) {
        currentSetlist = setlistItemList[position]
        presentationIterator = currentSetlist!!.songTitles.listIterator()
        currentSongTitle = presentationIterator!!.next()
        SongsContext.pickSong(currentSongTitle)
    }

    fun nextSlide(): Boolean {
        if (!SongsContext.nextSlide()) {
            if (!presentationIterator!!.hasNext()) {
                return false
            }
            if (presentationGap) {
                currentSongTitle = presentationIterator!!.next()
                SongsContext.pickSong(currentSongTitle)
            } else {
                currentSongTitle = ""
                presentationGap = true
            }
            return true
        } else {
            presentationGap = false
        }
        return false
    }

    fun previousSlide(): Boolean {
        if (!SongsContext.previousSlide()) {
            if (!presentationIterator!!.hasPrevious()) {
                return false
            }
            if (presentationGap) {
                currentSongTitle = presentationIterator!!.previous()
                SongsContext.pickSong(currentSongTitle)
                presentationGap = false
            } else {
                currentSongTitle = ""
                presentationGap = true
            }
            return true
        } else {
            presentationGap = false
        }
        return false
    }


    fun getCurrentSongTitle(): String {
        return currentSongTitle
    }

    fun getCurrentSlide(): String {
        if (currentSongTitle.isEmpty()) {
            return ""
        }

        return SongsContext.getCurrentSlide()
    }

    fun fillSetlistList(setlists: List<SetlistModel>) {
        setlistList = setlists.toMutableList()
        setlistItemList.clear()
        for (i in setlists.indices) {
            setlistItemList.add(SetlistItemModel(i, setlists[i]))
        }
        setlistListAdapter!!.setlists = setlistItemList
        setlistListAdapter!!.notifyDataSetChanged()
    }

    fun filterSetlists(name: String, category: String = "All") {
        setlistListAdapter!!.setlists = setlistItemList.filter { setlist ->
            setlist.name.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))
                    && (category == "All" || setlist.category == category)
        }.toMutableList()
        setlistListAdapter!!.notifyDataSetChanged()
    }

}