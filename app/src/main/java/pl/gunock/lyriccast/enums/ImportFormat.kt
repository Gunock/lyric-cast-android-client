/*
 * Created by Tomasz Kiljanczyk on 4/1/21 10:53 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 10:09 PM
 */

package pl.gunock.lyriccast.enums

enum class ImportFormat(private val textName: String) {
    NONE("NONE"),
    OPEN_SONG("OpenSong");

    companion object {
        fun getByName(name: String): ImportFormat {
            return values().first { it.textName == name }
        }
    }
}