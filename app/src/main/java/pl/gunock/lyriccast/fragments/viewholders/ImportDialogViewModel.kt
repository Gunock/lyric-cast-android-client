/*
 * Created by Tomasz Kiljanczyk on 4/1/21 10:53 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 10:31 PM
 */

package pl.gunock.lyriccast.fragments.viewholders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gunock.lyriccast.enums.ImportFormat

class ImportDialogViewModel(
    var importFormat: ImportFormat = ImportFormat.NONE,
    var deleteAll: Boolean = false,
    var replaceOnConflict: Boolean = false,
    val accepted: MutableLiveData<Boolean> = MutableLiveData(true)
) : ViewModel()