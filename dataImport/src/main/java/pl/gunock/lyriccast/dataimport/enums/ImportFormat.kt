/*
 * Created by Tomasz Kiljanczyk on 4/2/21 11:52 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/2/21 3:39 PM
 */

package pl.gunock.lyriccast.dataimport.enums

enum class ImportFormat(private val textName: String) {
    NONE("NONE"),
    OPEN_SONG("OpenSong");

    companion object {
        fun getByName(name: String): ImportFormat {
            return values().first { it.textName == name }
        }
    }
}