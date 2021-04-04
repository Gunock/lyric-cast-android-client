/*
 * Created by Tomasz Kiljanczyk on 4/4/21 2:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 12:30 AM
 */

package pl.gunock.lyriccast.dataimport.enums

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