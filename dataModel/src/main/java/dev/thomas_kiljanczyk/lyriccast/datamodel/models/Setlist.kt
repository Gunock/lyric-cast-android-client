/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models

import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SetlistDto

data class Setlist(
    var id: String,
    var name: String,
    var presentation: List<Song>
) {

    val idLong: Long = id.hashCode().toLong()

    internal constructor(dto: SetlistDto) : this("", dto.name, listOf())

    internal fun toDto(): SetlistDto {
        val songs: List<String> = presentation.map { it.title }
        return SetlistDto(name, songs)
    }
}