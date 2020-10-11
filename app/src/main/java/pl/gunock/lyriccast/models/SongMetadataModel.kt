/*
 * Created by Tomasz Kiljańczyk on 10/11/20 11:21 PM
 * Copyright (c) 2020 . All rights reserved.
 *  Last modified 10/11/20 10:13 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.utils.JsonHelper.arrayToStringList
import java.io.File
import java.util.*

class SongMetadataModel(json: JSONObject) {
    var lyricsFilename: String = json.getString("lyricsFilename")
    var title: String = json.getString("title")
    var author: String = json.getString("author")
    var copyright: String = json.getString("copyright")
    var tags: List<String> = arrayToStringList(json.getJSONArray("tags"))
    var presentation: List<String> = arrayToStringList(json.getJSONArray("presentation"))

    init {
        author = if (author.toLowerCase(Locale.ROOT) != "null") author else "Unknown"
        copyright = if (copyright.toLowerCase(Locale.ROOT) != "null") copyright else ""
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(title: $title, ")
        builder.append("author: $author, ")
        builder.append("copyright: $copyright, ")
        builder.append("tags: $tags)")

        return builder.toString()
    }

    fun loadLyrics(sourceDirectory: String): SongLyricsModel {
        val lyricsContent = File("$sourceDirectory$lyricsFilename").readText(Charsets.UTF_8)
        return SongLyricsModel(JSONObject(lyricsContent))
    }

}