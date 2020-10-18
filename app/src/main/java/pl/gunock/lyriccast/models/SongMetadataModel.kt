/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/17/20 12:18 PM
 */

package pl.gunock.lyriccast.models

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
    var tags: List<String> = listOf()
    var presentation: List<String> = listOf()

    constructor(json: JSONObject) : this() {
        lyricsFilename = json.getString("lyricsFilename")
        title = json.getString("title")
        author = json.getString("author")
        copyright = json.getString("copyright")
        category = json.getString("category")
        tags = arrayToStringList(json.getJSONArray("tags"))
        presentation = arrayToStringList(json.getJSONArray("presentation"))

        author = if (author.toLowerCase(Locale.ROOT) != "null") author else "Unknown"
        copyright = if (copyright.toLowerCase(Locale.ROOT) != "null") copyright else ""
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(title: $title, ")
        builder.append("author: $author)")

        return builder.toString()
    }

    fun loadLyrics(sourceDirectory: String): SongLyricsModel {
        val lyricsContent = File("$sourceDirectory$lyricsFilename").readText(Charsets.UTF_8)
        return SongLyricsModel(JSONObject(lyricsContent))
    }

}