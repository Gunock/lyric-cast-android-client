/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 1:02 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable
import pl.gunock.lyriccast.datatransfer.models.CategoryDto

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long? = null,
    val name: String = "",
    val color: Int? = null
) : Comparable<Category> {
    companion object {
        val ALL = Category(Long.MIN_VALUE, "All")
        val NONE = Category(Long.MIN_VALUE, "None")
    }

    val id: Long get() = categoryId.toNonNullable()

    constructor(categoryDto: CategoryDto) : this(null, categoryDto.name, categoryDto.color)

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

    fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

}
