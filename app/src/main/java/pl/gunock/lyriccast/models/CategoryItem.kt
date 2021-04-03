/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/27/21 11:34 PM
 */

package pl.gunock.lyriccast.models

import pl.gunock.lyriccast.datamodel.entities.Category

data class CategoryItem(
    val category: Category
) : Comparable<CategoryItem> {
    var isSelected: Boolean = false

    override fun compareTo(other: CategoryItem): Int {
        if (category.categoryId == other.category.categoryId) {
            return 0
        }

        return category.name.compareTo(other.category.name)
    }
}
