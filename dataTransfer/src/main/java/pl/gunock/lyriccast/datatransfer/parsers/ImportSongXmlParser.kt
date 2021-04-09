/*
 * Created by Tomasz Kiljanczyk on 4/9/21 12:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 11:18 AM
 */

package pl.gunock.lyriccast.datatransfer.parsers

import android.content.ContentResolver
import android.net.Uri
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.File
import java.io.InputStream

abstract class ImportSongXmlParser(filesDir: File) {
    protected val mImportDirectory: File = File(filesDir.canonicalPath, ".import")

    abstract fun parseZip(resolver: ContentResolver, targetUri: Uri): Set<SongDto>

    abstract fun parse(inputStream: InputStream?, category: String = ""): SongDto
}