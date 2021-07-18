/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.CategoryDto

data class Category(
    var name: String,
    var color: Int? = null,
    var id: String = ""
) : Comparable<Category> {

    val idLong: Long = id.hashCode().toLong()

    constructor(dto: CategoryDto) : this(dto.name, dto.color, "")

    fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

}