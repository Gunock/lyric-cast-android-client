/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:06
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.import_dialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.datatransfer.enums.ImportFormat
import javax.inject.Inject

@HiltViewModel
class ImportDialogModel @Inject constructor() : ViewModel() {

    var importFormat: ImportFormat = ImportFormat.NONE

    var deleteAll: Boolean = false

    var replaceOnConflict: Boolean = false

    override fun toString(): String {
        return "ImportDialogModel(importFormat=$importFormat, deleteAll=$deleteAll, replaceOnConflict=$replaceOnConflict)"
    }

}