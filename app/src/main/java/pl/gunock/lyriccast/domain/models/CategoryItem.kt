/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:18
 */

package pl.gunock.lyriccast.domain.models

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
