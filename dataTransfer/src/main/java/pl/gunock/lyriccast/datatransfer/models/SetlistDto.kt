/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:17 PM
 */

package pl.gunock.lyriccast.datatransfer.models

import org.json.JSONObject
import pl.gunock.lyriccast.datatransfer.extensions.getStringList

data class SetlistDto(val name: String, val songs: List<String>) {
    constructor(json: JSONObject) : this(json.getString("name"), json.getStringList("songs"))

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("songs", JSONObject.wrap(songs))
        }
    }
}