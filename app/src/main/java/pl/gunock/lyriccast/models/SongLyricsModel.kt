/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 8:48 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper.objectToMap

class SongLyricsModel() {
    var lyrics: Map<String, String> = mapOf()

    constructor(json: JSONObject) : this() {
        lyrics = objectToMap(json.getJSONObject("lyrics"))
    }
}