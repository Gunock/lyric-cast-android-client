/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/31/20 8:37 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper.arrayToStringList
import java.io.File
import java.util.*

class SongMetadataModel() {
    var lyricsFilename: String = ""
    var title: String = ""
    var author: String = ""
    var copyright: String = ""
    var category: String = ""
    var presentation: List<String> = listOf()

    constructor(json: JSONObject) : this() {
        lyricsFilename = json.getString("lyricsFilename")
        title = json.getString("title")
        author = json.getString("author")
        copyright = json.getString("copyright")
        category = json.getString("category")
        presentation = arrayToStringList(json.getJSONArray("presentation"))

        author = if (author.toLowerCase(Locale.ROOT) != "null") author else "Unknown"
        copyright = if (copyright.toLowerCase(Locale.ROOT) != "null") copyright else ""
    }

    fun loadLyrics(sourceDirectory: String): SongLyricsModel {
        val lyricsContent = File("$sourceDirectory$lyricsFilename").readText(Charsets.UTF_8)
        return SongLyricsModel(JSONObject(lyricsContent))
    }

    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("lyricsFilename", lyricsFilename)
        json.put("title", title)
        json.put("author", author)
        json.put("copyright", copyright)
        json.put("category", category)
        json.put("presentation", JSONArray(presentation))
        return json
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(title: $title, ")
        builder.append("author: $author)")

        return builder.toString()
    }

}