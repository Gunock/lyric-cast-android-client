/*
 * Created by Tomasz Kiljańczyk on 2/28/21 10:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/28/21 12:47 PM
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

    private var _category: String? = null
    var category: String?
        get() = _category
        set(value) {
            _category = if (value.isNullOrBlank() || value == null.toString()) null else value
        }
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
        return JSONObject().apply {
            put("lyricsFilename", lyricsFilename)
            put("title", title)
            put("author", author)
            put("copyright", copyright)
            put("category", _category ?: JSONObject.NULL)
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