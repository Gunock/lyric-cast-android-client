/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 10:03
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 09:02
 */

package pl.gunock.lyriccast.domain.models

import pl.gunock.lyriccast.datamodel.models.Category

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
