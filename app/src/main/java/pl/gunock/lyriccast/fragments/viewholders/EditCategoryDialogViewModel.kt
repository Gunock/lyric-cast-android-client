/*
 * Created by Tomasz Kiljanczyk on 4/1/21 10:53 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 10:08 PM
 */

package pl.gunock.lyriccast.fragments.viewholders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datamodel.entities.Category

class EditCategoryDialogViewModel(
    val categoryNames: MutableLiveData<Set<String>> = MutableLiveData(setOf()),
    var category: MutableLiveData<Category> = MutableLiveData<Category>(),
) : ViewModel()