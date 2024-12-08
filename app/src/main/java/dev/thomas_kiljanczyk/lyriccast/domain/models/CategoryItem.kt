/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.domain.models

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category

data class CategoryItem(
    val category: Category,
    var hasCheckbox: Boolean = false,
    var isSelected: Boolean = false
) : Comparable<CategoryItem> {
    override fun compareTo(other: CategoryItem): Int {
        return category.name.compareTo(other.category.name)
    }

    override fun toString(): String {
        return category.name
    }
}
