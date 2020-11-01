/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/31/20 8:37 PM
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
        val json = JSONObject()
        json.put("lyrics", JSONObject(lyrics))
        return json
    }
}