/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/27/21 11:06 PM
 */

package pl.gunock.lyriccast.misc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datamodel.entities.Category

class EditCategoryViewModel(
    val categoryNames: MutableLiveData<Set<String>> = MutableLiveData(setOf()),
    var category: MutableLiveData<Category> = MutableLiveData<Category>(),
) : ViewModel()