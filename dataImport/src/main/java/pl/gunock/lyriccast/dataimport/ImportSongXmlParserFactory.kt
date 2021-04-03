/*
 * Created by Tomasz Kiljanczyk on 4/1/21 10:53 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/30/21 9:02 PM
 */

package pl.gunock.lyriccast.dataimport

import pl.gunock.lyriccast.dataimport.enums.SongXmlParserType
import pl.gunock.lyriccast.dataimport.parsers.ImportSongXmlParser
import pl.gunock.lyriccast.dataimport.parsers.OpenSongXmlParser
import java.io.File

object ImportSongXmlParserFactory {

    fun create(fileDir: File, type: SongXmlParserType): ImportSongXmlParser {
        return when (type) {
            SongXmlParserType.OPEN_SONG -> OpenSongXmlParser(fileDir)
        }
    }

}