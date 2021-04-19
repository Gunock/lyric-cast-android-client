/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 4:41 PM
 */

package pl.gunock.lyriccast.fragments.viewholders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datamodel.entities.CategoryDocument

class EditCategoryDialogViewModel(
    val categoryNames: MutableLiveData<Set<String>> = MutableLiveData(setOf()),
    var category: MutableLiveData<CategoryDocument> = MutableLiveData<CategoryDocument>(),
) : ViewModel()