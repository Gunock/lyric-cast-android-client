/*
 * Created by Tomasz Kiljanczyk on 4/4/21 2:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 1:04 AM
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.dataimport.enums.ImportFormat

data class ImportSongsOptions(
    val importFormat: ImportFormat = ImportFormat.NONE,
    val deleteAll: Boolean = false,
    val replaceOnConflict: Boolean = false,
    @Suppress("ArrayInDataClass")
    val colors: IntArray = intArrayOf()
)
