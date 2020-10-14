/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 8:49 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper

class SetlistModel() {
    var name: String = ""
    var songs: List<SongMetadataModel> = listOf()

    constructor(json: JSONObject) : this() {
        name = json.getString("name")

        val songsMetadata: List<JSONObject> = JsonHelper.arrayToJsonList(json.getJSONArray("songs"))
        songs = List(songsMetadata.size) {
            SongMetadataModel(songsMetadata[it])
        }
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(name: $name, ")
        builder.append("songs: $songs)")

        return builder.toString()
    }

}