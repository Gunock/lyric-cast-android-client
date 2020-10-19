/*
 * Created by Tomasz Kiljańczyk on 10/19/20 4:40 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/19/20 4:35 PM
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

    var setlistList: MutableList<SetlistModel> = mutableListOf()
    var setlistListAdapter: SetlistListAdapter? = null

    fun loadSetlists() {
        val loadedSetlists: MutableList<SetlistModel> = mutableListOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(setlistsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return
        }

        for (file in fileList) {
            if (file.isDirectory) {
                file.deleteRecursively()
            }

            Log.d(tag, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val songSetlist = SetlistModel(json)
            loadedSetlists.add(songSetlist)
        }
        Log.d(tag, "Parsed metadata files: $loadedSetlists")

        setlistList = loadedSetlists
    }

    fun saveSetlist(setlist: SetlistModel) {
        val json = setlist.toJson()
        val setlistFile = File("$setlistsDirectory/${setlist.name}.json")
        File(setlistsDirectory).mkdirs()
        setlistFile.writeText(json.toString())
    }

    fun filter(name: String, category: String = "All") {
        setlistListAdapter!!.setlists = setlistList.filter { setlist ->
            setlist.name.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))
                    && (category == "All" || setlist.category == category)
        }.toMutableList()
        setlistListAdapter!!.notifyDataSetChanged()
    }

}