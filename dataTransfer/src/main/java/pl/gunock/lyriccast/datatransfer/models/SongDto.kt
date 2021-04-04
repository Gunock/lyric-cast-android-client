/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:45 AM
 */

package pl.gunock.lyriccast.datatransfer.models

import org.json.JSONObject
import pl.gunock.lyriccast.datamodel.extensions.getStringList
import pl.gunock.lyriccast.datamodel.extensions.toStringMap


data class SongDto(
    val title: String,
    val lyrics: Map<String, String>,
    val presentation: List<String>,
    val category: String = ""
) {
    constructor(json: JSONObject) : this(
        json.getString("title"),
        json.getJSONObject("lyrics").toStringMap(),
        json.getStringList("presentation"),
        json.getString("category")
    )

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("title", title)
            put("lyrics", JSONObject.wrap(lyrics))
            put("presentation", JSONObject.wrap(presentation))
            put("category", category)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is SongDto) {
            return false
        }
        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }
}