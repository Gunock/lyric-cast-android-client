/*
 * Created by Tomasz Kiljanczyk on 4/1/21 10:53 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/31/21 1:01 AM
 */

package pl.gunock.lyriccast.dataimport.models


data class ImportSong(
    val title: String,
    val presentation: List<String>,
    val lyrics: Map<String, String>,
    val category: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ImportSong) {
            return false
        }
        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }
}