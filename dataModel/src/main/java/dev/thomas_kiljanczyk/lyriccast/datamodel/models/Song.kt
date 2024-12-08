/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models

import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SongDto

data class Song(
    var id: String = "",
    var title: String,
    var lyrics: List<LyricsSection>,
    var presentation: List<String>,
    var category: Category? = null
) {

    data class LyricsSection(
        var name: String,
        var text: String
    )

    val idLong: Long = id.hashCode().toLong()

    val lyricsMap: Map<String, String> = lyrics.associate { it.name to it.text }

    val lyricsList: List<String> = presentation.map { lyricsMap[it]!! }

    constructor() : this(
        title = "", lyrics = listOf(),
        presentation = listOf(),
        category = null
    )

    internal constructor(dto: SongDto, category: Category?) : this(
        id = "",
        title = dto.title,
        lyrics = listOf(),
        presentation = listOf(),
        category = category
    )

    fun toDto(): SongDto {
        return SongDto(title, lyricsMap, presentation.toList(), category?.name ?: "")
    }
}

