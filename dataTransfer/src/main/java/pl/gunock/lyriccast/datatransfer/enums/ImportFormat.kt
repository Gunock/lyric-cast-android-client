/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:17 PM
 */

package pl.gunock.lyriccast.datatransfer.enums

enum class ImportFormat(private val mTextName: String) {
    NONE("NONE"),
    OPEN_SONG("OpenSong"),
    LYRIC_CAST("LyricCast");

    companion object {
        fun getByName(name: String): ImportFormat {
            return values().first { it.mTextName == name }
        }
    }
}