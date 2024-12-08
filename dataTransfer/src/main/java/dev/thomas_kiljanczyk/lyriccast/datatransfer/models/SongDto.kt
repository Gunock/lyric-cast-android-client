/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.models

import dev.thomas_kiljanczyk.lyriccast.datatransfer.extensions.getStringList
import dev.thomas_kiljanczyk.lyriccast.datatransfer.extensions.toStringMap
import org.json.JSONObject


data class SongDto(
    val title: String,
    val lyrics: Map<String, String>,
    val presentation: List<String>,
    val category: String? = null
) {
    constructor(json: JSONObject) : this(
        json.getString("title"),
        json.getJSONObject("lyrics").toStringMap(),
        json.getStringList("presentation"),
        json.optString("category")
    )

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("title", title)
            put("lyrics", JSONObject.wrap(lyrics))
            put("presentation", JSONObject.wrap(presentation))
            put("category", JSONObject.wrap(category))
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