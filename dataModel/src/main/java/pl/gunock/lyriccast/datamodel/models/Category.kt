/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 20:14
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.CategoryDto

open class Category(
    var name: String,
    var color: Int? = null,
    var id: String = ""
) : Comparable<Category> {

    val idLong: Long = id.hashCode().toLong()

    constructor(dto: CategoryDto) : this(dto.name, dto.color, "")

    constructor(document: Category, id: String) : this(document.name, document.color, id)

    fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

}