/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:51 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper

open class SetlistModel() : Comparable<SetlistModel> {

    var name: String = ""
    var songTitles: List<String> = listOf()
    var category: String = ""

    constructor(json: JSONObject) : this() {
        name = json.getString("name")
        songTitles = JsonHelper.arrayToStringList(json.getJSONArray("songTitles"))
    }

    override fun toString(): String {
        return StringBuilder().apply {
            append("(name: $name, ")
            append("songs: $songTitles)")
        }.toString()
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("songTitles", JSONArray(songTitles))
        }
    }

    override fun compareTo(other: SetlistModel): Int {
        return name.compareTo(other.name)
    }
}