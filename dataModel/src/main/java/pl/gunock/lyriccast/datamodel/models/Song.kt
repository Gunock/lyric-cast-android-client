/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 20:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 17:45
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.SongDto

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

    val lyricsMap: Map<String, String> = lyrics.map { it.name to it.text }.toMap()

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

