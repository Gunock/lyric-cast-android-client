/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:23 AM
 */

package pl.gunock.lyriccast.datatransfer.enums

enum class ImportFormat(private val textName: String) {
    NONE("NONE"),
    OPEN_SONG("OpenSong"),
    LYRIC_CAST("LyricCast");

    companion object {
        fun getByName(name: String): ImportFormat {
            return values().first { it.textName == name }
        }
    }
}