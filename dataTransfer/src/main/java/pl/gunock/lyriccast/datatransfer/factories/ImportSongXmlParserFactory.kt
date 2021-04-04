/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:25 AM
 */

package pl.gunock.lyriccast.datatransfer.factories

import pl.gunock.lyriccast.datatransfer.enums.SongXmlParserType
import pl.gunock.lyriccast.datatransfer.parsers.ImportSongXmlParser
import pl.gunock.lyriccast.datatransfer.parsers.OpenSongXmlParser
import java.io.File

object ImportSongXmlParserFactory {

    fun create(fileDir: File, type: SongXmlParserType): ImportSongXmlParser {
        return when (type) {
            SongXmlParserType.OPEN_SONG -> OpenSongXmlParser(fileDir)
        }
    }

}