/*
 * Created by Tomasz Kiljanczyk on 4/20/21 5:24 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 5:24 PM
 */

package pl.gunock.lyriccast.fragments.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat

data class ImportDialogViewModel(
    var importFormat: ImportFormat = ImportFormat.NONE,
    var deleteAll: Boolean = false,
    var replaceOnConflict: Boolean = false,
    val accepted: MutableLiveData<Boolean> = MutableLiveData(true)
) : ViewModel()