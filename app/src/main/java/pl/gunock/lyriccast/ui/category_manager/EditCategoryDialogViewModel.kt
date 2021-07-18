/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 20:10
 */

package pl.gunock.lyriccast.ui.category_manager

import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datamodel.models.Category

class EditCategoryDialogViewModel(
    var categoryNames: Set<String> = setOf(),
    var category: Category? = null,
) : ViewModel()