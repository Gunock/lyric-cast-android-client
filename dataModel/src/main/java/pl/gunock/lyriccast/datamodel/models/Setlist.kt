/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.SetlistDto

data class Setlist(
    var name: String,
    var presentation: List<Song>,
    var id: String
) {

    val idLong: Long = id.hashCode().toLong()

    constructor(dto: SetlistDto) : this(dto.name, listOf(), "")

    fun toDto(): SetlistDto {
        val songs: List<String> = presentation.map { it.title }
        return SetlistDto(name, songs)
    }
}