/*
 * Created by Tomasz Kiljanczyk on 4/2/21 11:52 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/2/21 11:49 AM
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.dataimport.enums.ImportFormat

data class ImportSongsOptions(
    val importFormat: ImportFormat = ImportFormat.NONE,
    val deleteAll: Boolean = false,
    val replaceOnConflict: Boolean = false,
    @Suppress("ArrayInDataClass")
    val colors: IntArray
)
