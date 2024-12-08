/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.models

import dev.thomas_kiljanczyk.lyriccast.datatransfer.extensions.getStringList
import org.json.JSONObject

data class SetlistDto(val name: String, val songs: List<String>) {
    constructor(json: JSONObject) : this(json.getString("name"), json.getStringList("songs"))

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("songs", JSONObject.wrap(songs))
        }
    }
}