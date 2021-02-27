/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 11:51 AM
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
    private const val TAG = "SongsContext"

    var setlistsDirectory: String = ""

    var setlistList: SortedSet<SetlistModel> = sortedSetOf()

    // TODO: Try to move adapters to activities/fragments
    var setlistItemList: MutableList<SetlistItemModel> = mutableListOf()

    fun loadSetlists(): SortedSet<SetlistModel> {
        val loadedSetlists: SortedSet<SetlistModel> = sortedSetOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(setlistsDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return sortedSetOf()
        }

        for (file in fileList) {
            Log.d(TAG, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val setlist = SetlistModel(json)
            loadedSetlists.add(setlist)
        }
        Log.d(TAG, "Parsed setlist files: $loadedSetlists")

        return loadedSetlists
    }

    fun saveSetlist(setlist: SetlistModel) {
        val json = setlist.toJson()
        val setlistFile = File("$setlistsDirectory/${setlist.name}.json")
        File(setlistsDirectory).mkdirs()
        setlistFile.writeText(json.toString())
    }

    fun deleteSetlists(setlistNames: List<String>) {
        for (setlistName in setlistNames) {
            val setlistFile = File("$setlistsDirectory/${setlistName}.json")
            setlistFile.delete()
        }
        setlistList = setlistList.filter { setlist ->
            !setlistNames.contains(setlist.name)
        }.toSortedSet()
    }

    fun getSetlist(setlistName: String): SetlistModel? {
        return setlistList.first { setlist -> setlist.name == setlistName }
    }

}