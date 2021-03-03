/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:03 PM
 */

package pl.gunock.lyriccast.models

import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.helpers.JsonHelper.arrayToStringList
import java.io.File
import java.util.*

class SongMetadata() {
    private var lyricsFilename: String = ""

    private var _title: String = ""
    var title: String
        get() = _title
        set(value) {
            _title = value
            lyricsFilename = "${title.normalize()}.json"
        }

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
        _title = json.getString("title")
        author = json.getString("author")
        copyright = json.getString("copyright")
        category = json.getString("category")
        presentation = arrayToStringList(json.getJSONArray("presentation"))

        author = if (author.toLowerCase(Locale.ROOT) != "null") author else "Unknown"
        copyright = if (copyright.toLowerCase(Locale.ROOT) != "null") copyright else ""
    }

    fun loadLyrics(sourceDirectory: String): SongLyrics {
        val lyricsContent = File("$sourceDirectory$lyricsFilename").readText(Charsets.UTF_8)
        return SongLyrics(JSONObject(lyricsContent))
    }

    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("lyricsFilename", lyricsFilename)
            put("title", _title)
            put("author", author)
            put("copyright", copyright)
            put("category", _category ?: JSONObject.NULL)
            put("presentation", JSONArray(presentation))
        }
    }

    override fun toString(): String {
        return StringBuilder().apply {
            append("(title: $_title, ")
            append("author: $author)")
        }.toString()
    }

}