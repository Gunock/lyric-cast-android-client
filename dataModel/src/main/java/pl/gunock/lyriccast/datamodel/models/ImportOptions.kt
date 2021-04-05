/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:17 PM
 */

package pl.gunock.lyriccast.datamodel.models

class ImportOptions(
    val deleteAll: Boolean = false,
    val replaceOnConflict: Boolean = false,
    val colors: IntArray = intArrayOf()
)
