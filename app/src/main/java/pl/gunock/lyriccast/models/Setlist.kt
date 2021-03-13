/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 2:44 PM
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
        if (id == other.id) {
            return 0
        }

        return name.compareTo(other.name)
    }
}