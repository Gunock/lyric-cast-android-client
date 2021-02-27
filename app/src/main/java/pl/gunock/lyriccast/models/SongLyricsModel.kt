/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:51 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper.objectToMap

class SongLyricsModel() {
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