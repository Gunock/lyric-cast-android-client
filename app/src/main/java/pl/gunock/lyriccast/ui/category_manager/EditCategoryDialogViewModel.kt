/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:30
 */

package pl.gunock.lyriccast.ui.category_manager

import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument

class EditCategoryDialogViewModel(
    var categoryNames: Set<String> = setOf(),
    var category: CategoryDocument? = null,
) : ViewModel()