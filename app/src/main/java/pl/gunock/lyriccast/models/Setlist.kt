/*
 * Created by Tomasz Kiljańczyk on 3/12/21 4:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 1:24 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.extensions.getLongArray

open class Setlist(
    val id: Long,
    val name: String,
    var songIds: List<Long>
) : Comparable<Setlist> {

    constructor(json: JSONObject) : this(
        json.getLong("id"),
        json.getString("name"),
        json.getLongArray("songIds").toList()
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