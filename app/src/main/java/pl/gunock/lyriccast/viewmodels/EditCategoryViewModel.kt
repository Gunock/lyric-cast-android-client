/*
 * Created by Tomasz Kiljańczyk on 3/7/21 11:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/7/21 10:37 PM
 */

package pl.gunock.lyriccast.viewmodels

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