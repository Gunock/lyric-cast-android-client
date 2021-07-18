/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 14:17
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.SetlistDto

open class Setlist(
    var name: String,
    var presentation: List<Song>,
    var id: String
) {

    val idLong: Long = id.hashCode().toLong()


    constructor(dto: SetlistDto) : this(dto.name, listOf(), "")

    constructor(document: Setlist, id: String) : this(
        document.name,
        document.presentation,
        id
    )

    fun toDto(): SetlistDto {
        val songs: List<String> = presentation.map { it.title }
        return SetlistDto(name, songs)
    }
}