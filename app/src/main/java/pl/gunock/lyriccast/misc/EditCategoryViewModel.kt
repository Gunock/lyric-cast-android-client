/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:26 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 1:26 AM
 */

package pl.gunock.lyriccast.misc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.models.CategoryItem

class EditCategoryViewModel(
    var category: MutableLiveData<CategoryDto> = MutableLiveData<CategoryDto>(),
) : ViewModel() {

    class CategoryDto(
        val category: CategoryItem,
        val oldCategory: CategoryItem?
    )

}