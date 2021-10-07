/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 12:51
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 12:36
 */

package pl.gunock.lyriccast.datamodel.models

data class ImportOptions(
    val deleteAll: Boolean = false,
    val replaceOnConflict: Boolean = false,
    val colors: IntArray = intArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImportOptions

        if (deleteAll != other.deleteAll) return false
        if (replaceOnConflict != other.replaceOnConflict) return false
        if (!colors.contentEquals(other.colors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deleteAll.hashCode()
        result = 31 * result + replaceOnConflict.hashCode()
        result = 31 * result + colors.contentHashCode()
        return result
    }
}
