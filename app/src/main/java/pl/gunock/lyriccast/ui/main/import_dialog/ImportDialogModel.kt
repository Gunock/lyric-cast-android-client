/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 12:51
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 12:48
 */

package pl.gunock.lyriccast.ui.main.import_dialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat
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