/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/22/21 8:16 PM
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
    private val setlistItemList: MutableList<SetlistModel> = mutableListOf()
    var setlistListAdapter: SetlistListAdapter? = null

    var currentSetlist: SetlistModel? = null
    private var presentationIterator: ListIterator<Pair<String, String>>? = null

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


    var slideList: MutableList<Pair<String, String>> = mutableListOf()

    fun pickSetlist(position: Int) {
        currentSetlist = setlistListAdapter!!.setlists[position]

        slideList.clear()
        for (songTitle in currentSetlist!!.songTitles) {
//            slideList.add(Pair(songTitle, ""))

            val songMetadata = SongsContext.songMap[songTitle]!!
            val songLyrics = SongsContext.getSongLyrics(songTitle).lyrics

            for (section in songMetadata.presentation) {
                val lyrics = songLyrics[section] ?: error("ERROR")
                slideList.add(Pair(songTitle, lyrics))
            }
        }

        presentationIterator = slideList.listIterator()
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