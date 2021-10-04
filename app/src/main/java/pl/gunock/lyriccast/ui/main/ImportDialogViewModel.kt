/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 15:38
 */

package pl.gunock.lyriccast.ui.main

import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat

data class ImportDialogViewModel(
    var importFormat: ImportFormat = ImportFormat.NONE,
    var deleteAll: Boolean = false,
    var replaceOnConflict: Boolean = false
) : ViewModel()