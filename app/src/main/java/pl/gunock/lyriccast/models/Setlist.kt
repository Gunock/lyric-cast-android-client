/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:03 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.helpers.JsonHelper

open class Setlist() : Comparable<Setlist> {

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

    override fun compareTo(other: Setlist): Int {
        return name.compareTo(other.name)
    }
}