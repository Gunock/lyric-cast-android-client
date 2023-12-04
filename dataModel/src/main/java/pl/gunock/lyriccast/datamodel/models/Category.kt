/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 20:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 17:43
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.CategoryDto

data class Category(
    var name: String,
    var color: Int? = null,
    var id: String = ""
) : Comparable<Category> {

    val idLong: Long = id.hashCode().toLong()

    internal constructor(dto: CategoryDto) : this(dto.name, dto.color, "")

    internal fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

}