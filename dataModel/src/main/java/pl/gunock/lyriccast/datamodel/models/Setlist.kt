/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 20:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 17:43
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.SetlistDto

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