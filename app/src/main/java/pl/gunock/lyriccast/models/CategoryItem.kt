/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 4:41 PM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.entities.CategoryDocument

data class CategoryItem(
    val category: CategoryDocument
) : Comparable<CategoryItem> {
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: CategoryItem): Int {
        return category.name.compareTo(other.category.name)
    }
}
