/*
 * Created by Tomasz Kiljanczyk on 4/1/21 11:57 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 11:24 PM
 */

package pl.gunock.lyriccast.fragments.viewholders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.enums.ImportFormat

data class ImportDialogViewModel(
    var importFormat: ImportFormat = ImportFormat.NONE,
    var deleteAll: Boolean = false,
    var replaceOnConflict: Boolean = false,
    val accepted: MutableLiveData<Boolean> = MutableLiveData(true)
) : ViewModel()