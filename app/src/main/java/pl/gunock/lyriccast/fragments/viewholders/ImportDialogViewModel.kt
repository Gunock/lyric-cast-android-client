/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:23 AM
 */

package pl.gunock.lyriccast.fragments.viewholders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat

data class ImportDialogViewModel(
    var importFormat: ImportFormat = ImportFormat.NONE,
    var deleteAll: Boolean = false,
    var replaceOnConflict: Boolean = false,
    val accepted: MutableLiveData<Boolean> = MutableLiveData(true)
) : ViewModel()