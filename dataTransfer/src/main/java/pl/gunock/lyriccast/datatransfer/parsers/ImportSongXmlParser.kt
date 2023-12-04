/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.datatransfer.parsers

import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.File
import java.io.InputStream

abstract class ImportSongXmlParser(filesDir: File) {
    protected val importDirectory: File = File(filesDir.canonicalPath, ".import")

    abstract fun parseZip(inputStream: InputStream): Set<SongDto>

    abstract fun parse(inputStream: InputStream?, category: String = ""): SongDto
}