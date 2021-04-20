/*
 * Created by Tomasz Kiljanczyk on 4/20/21 10:45 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 10:29 PM
 */

package pl.gunock.lyriccast.fragments.viewmodels

import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument

class EditCategoryDialogViewModel(
    var categoryNames: Set<String> = setOf(),
    var category: CategoryDocument? = null,
) : ViewModel()