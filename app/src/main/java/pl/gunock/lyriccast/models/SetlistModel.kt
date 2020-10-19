/*
 * Created by Tomasz Kiljańczyk on 10/19/20 4:40 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/19/20 4:35 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper

class SetlistModel() {
    var name: String = ""
    var songTitles: List<String> = listOf()
    var category: String = ""

    constructor(json: JSONObject) : this() {
        name = json.getString("name")
        songTitles = JsonHelper.arrayToStringList(json.getJSONArray("songTitles"))
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(name: $name, ")
        builder.append("songs: $songTitles)")

        return builder.toString()
    }

    fun toJson(): JSONObject {
        val result = JSONObject()
        result.put("name", name)
        result.put("songTitles", JSONArray(songTitles))
        return result
    }
}