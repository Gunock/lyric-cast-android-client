/*
 * Created by Tomasz Kiljańczyk on 3/9/21 2:21 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 1:29 AM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.extensions.toMap

class SongLyrics() {
    var lyrics: Map<String, String> = mapOf()

    constructor(json: JSONObject) : this() {
        lyrics = json.getJSONObject("lyrics").toMap()
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("lyrics", JSONObject(lyrics))
        }
    }

}