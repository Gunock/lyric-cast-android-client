/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:03 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.helpers.JsonHelper.objectToMap

class SongLyrics() {
    var lyrics: Map<String, String> = mapOf()

    constructor(json: JSONObject) : this() {
        lyrics = objectToMap(json.getJSONObject("lyrics"))
    }

    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("lyrics", JSONObject(lyrics))
        }
    }

}