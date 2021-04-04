/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:54 AM
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.enums.ImportFormat

data class ImportOptions(
    val importFormat: ImportFormat = ImportFormat.NONE,
    val deleteAll: Boolean = false,
    val replaceOnConflict: Boolean = false,
    @Suppress("ArrayInDataClass")
    val colors: IntArray = intArrayOf()
)
