/*
 * Created by Tomasz Kiljańczyk on 10/11/20 11:21 PM
 * Copyright (c) 2020 . All rights reserved.
 *  Last modified 10/11/20 2:11 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper.objectToMap

class SongLyricsModel(json: JSONObject) {
    var lyrics: Map<String, String> = objectToMap(json.getJSONObject("lyrics"))
}