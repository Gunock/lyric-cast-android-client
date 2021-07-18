/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 13:02
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

    val idLong: Long = id.hashCode().toLong()

    val lyricsMap: Map<String, String> = lyrics.map { it.name to it.text }.toMap()

    val lyricsList: List<String> = presentation.map { lyricsMap[it]!! }


    fun toDto(): SongDto {
        return SongDto(title, lyricsMap, presentation.toList(), category?.name ?: "")
    }


    data class LyricsSection(
        var name: String,
        var text: String
    )
}

