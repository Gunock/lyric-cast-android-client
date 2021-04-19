/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 12:11 AM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument

data class CategoryItem(
    val category: CategoryDocument
) : Comparable<CategoryItem> {
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: CategoryItem): Int {
        return category.name.compareTo(other.category.name)
    }
}
