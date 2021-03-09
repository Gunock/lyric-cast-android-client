/*
 * Created by Tomasz Kiljańczyk on 3/9/21 1:07 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 1:06 AM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.helpers.JsonHelper.arrayToStringList
import java.io.File

class SongMetadata(val id: Long) {
    private var lyricsFilename: String = "$id.json"
    var title: String = ""
    var author: String = ""
    var categoryId: Long = Long.MIN_VALUE
    var presentation: List<String> = listOf()

    constructor(json: JSONObject) : this(json.getLong("id")) {
        title = json.getString("title")
        author = json.getString("author")
        categoryId = json.getLong("category")
        presentation = arrayToStringList(json.getJSONArray("presentation"))

        author = if (!author.equals("null", true)) author else "Unknown"
    }

    fun loadLyrics(sourceDirectory: String): SongLyrics {
        val lyricsContent = File("$sourceDirectory$lyricsFilename").readText(Charsets.UTF_8)
        return SongLyrics(JSONObject(lyricsContent))
    }

    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("author", author)
            put("category", categoryId)
            put("presentation", JSONArray(presentation))
        }
    }

    override fun toString(): String {
        return StringBuilder().apply {
            append("(title: $title, ")
            append("author: $author)")
        }.toString()
    }

}