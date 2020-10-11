/*
 * Created by Tomasz Kiljańczyk on 10/11/20 11:21 PM
 * Copyright (c) 2020 . All rights reserved.
 *  Last modified 10/11/20 12:18 PM
 */

package pl.gunock.lyriccast.utils

import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileHelper {
    private const val tag = "FileHelper"


    fun unzip(resolver: ContentResolver, inputStream: InputStream, targetLocation: String) {
        Log.d(tag, "Unzipping stream to '$targetLocation'")
        createDirIfNotExists(targetLocation)

        val zin = ZipInputStream(inputStream)
        var ze: ZipEntry?

        while (zin.nextEntry.also { ze = it } != null) {
            if (ze!!.isDirectory) {
                continue
            }

            Log.d(tag, "Extracting file: ${ze!!.name}")

            val filename: String = ze!!.name.split("/").last()
            Log.d(tag, "Extracting file to: ${targetLocation + filename}")

            resolver.openOutputStream(File(targetLocation + filename).toUri()).use {
                it!!.write(zin.readBytes())
                zin.closeEntry()
            }
            Log.d(tag, "File extracted to: ${targetLocation + filename}")
        }
        zin.close()
    }

    private fun createDirIfNotExists(path: String) {
        val file = File(path)
        if (!file.exists()) {
            Log.d(tag, "'$path' does not exists")
            if (File(path).mkdirs()) {
                Log.d(tag, "'$path' directory creation succeeded")
            } else {
                Log.d(tag, "'$path' directory creation failed")
            }
        }
    }
}