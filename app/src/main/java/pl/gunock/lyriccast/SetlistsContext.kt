/*
 * Created by Tomasz Kiljańczyk on 11/1/20 9:57 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 9:57 PM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.models.SetlistModel
import java.io.File
import java.io.FilenameFilter
import java.util.*

object SetlistsContext {
    private const val tag = "SongsContext"

    var setlistsDirectory: String = ""

    var setlistList: SortedSet<SetlistModel> = sortedSetOf()

    // TODO: Try to move adapters to activities/fragments
    val setlistItemList: MutableList<SetlistModel> = mutableListOf()
    var setlistListAdapter: SetlistListAdapter? = null

    var currentSetlist: SetlistModel? = null
    private var presentationIterator: ListIterator<String>? = null
    private var currentSongTitle: String = ""
    private var presentationGap: Boolean = false

    fun loadSetlists(): SortedSet<SetlistModel> {
        val loadedSetlists: SortedSet<SetlistModel> = sortedSetOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(setlistsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return sortedSetOf()
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
            setlistItemList.add(setlistList.elementAt(i))
        }
        setlistListAdapter = SetlistListAdapter(setlistItemList)
    }

    fun pickSetlist(position: Int) {
        currentSetlist = setlistListAdapter!!.setlists[position]
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

    fun fillSetlistList(setlists: SortedSet<SetlistModel>) {
        setlistList = setlists
        setlistItemList.clear()
        for (i in setlists.indices) {
            setlistItemList.add(setlists.elementAt(i))
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