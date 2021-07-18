/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:30
 */

package pl.gunock.lyriccast.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat

data class ImportDialogViewModel(
    var importFormat: ImportFormat = ImportFormat.NONE,
    var deleteAll: Boolean = false,
    var replaceOnConflict: Boolean = false,
    val accepted: MutableLiveData<Boolean> = MutableLiveData(true)
) : ViewModel()