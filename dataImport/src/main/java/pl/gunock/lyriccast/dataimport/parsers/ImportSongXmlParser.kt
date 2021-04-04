/*
 * Created by Tomasz Kiljanczyk on 4/4/21 12:28 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/3/21 11:16 PM
 */

package pl.gunock.lyriccast.dataimport.parsers

import android.content.ContentResolver
import pl.gunock.lyriccast.dataimport.models.ImportSong
import java.io.File
import java.io.InputStream

abstract class ImportSongXmlParser(filesDir: File) {
    protected val importDirectory: File = File(filesDir.canonicalPath, ".import")

    abstract fun parseZip(resolver: ContentResolver, inputStream: InputStream): Set<ImportSong>

    abstract fun parse(inputStream: InputStream?, category: String = ""): ImportSong
}