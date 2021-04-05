/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:13 PM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.entities.Category

data class CategoryItem(
    val category: Category
) : Comparable<CategoryItem> {
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: CategoryItem): Int {
        if (category.categoryId == other.category.categoryId) {
            return 0
        }

        return category.name.compareTo(other.category.name)
    }
}
