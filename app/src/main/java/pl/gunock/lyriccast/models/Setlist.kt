/*
 * Created by Tomasz Kiljańczyk on 3/9/21 1:07 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 12:31 AM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.helpers.JsonHelper

open class Setlist(
    val id: Long,
    val name: String,
    var songIds: List<Long>
) : Comparable<Setlist> {

    var category: String = ""

    constructor(json: JSONObject) : this(
        json.getLong("id"),
        json.getString("name"),
        JsonHelper.arrayToLongList(json.getJSONArray("songIds"))
    )

    override fun toString(): String {
        return StringBuilder().apply {
            append("(name: $name, ")
            append("songs: $songIds)")
        }.toString()
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("songIds", JSONArray(songIds))
        }
    }

    override fun compareTo(other: Setlist): Int {
        return name.compareTo(other.name)
    }
}