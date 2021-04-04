/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:25 AM
 */

package pl.gunock.lyriccast.datatransfer.parsers

import android.content.ContentResolver
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.File
import java.io.InputStream

abstract class ImportSongXmlParser(filesDir: File) {
    protected val importDirectory: File = File(filesDir.canonicalPath, ".import")

    abstract fun parseZip(resolver: ContentResolver, inputStream: InputStream): Set<SongDto>

    abstract fun parse(inputStream: InputStream?, category: String = ""): SongDto
}