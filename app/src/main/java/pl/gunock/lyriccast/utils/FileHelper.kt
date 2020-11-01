/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/25/20 9:36 PM
 */

package pl.gunock.lyriccast.utils

import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileHelper {
    private const val tag = "FileHelper"

    fun unzip(resolver: ContentResolver, inputStream: InputStream, targetLocation: String) {
        Log.d(tag, "Unzipping stream to '$targetLocation'")
        createDirIfNotExists(targetLocation)

        val zipIn = ZipInputStream(inputStream)
        var zipEntry: ZipEntry?

        while (zipIn.nextEntry.also { zipEntry = it } != null) {
            if (zipEntry!!.isDirectory) {
                continue
            }

            Log.d(tag, "Extracting file: ${zipEntry!!.name}")

            val filename: String = zipEntry!!.name.split("/").last()
            Log.d(tag, "Extracting file to: ${targetLocation + filename}")

            resolver.openOutputStream(File(targetLocation + filename).toUri()).use {
                it!!.write(zipIn.readBytes())
                zipIn.closeEntry()
            }
            Log.d(tag, "File extracted to: ${targetLocation + filename}")
        }
        zipIn.close()
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun zip(outputStream: OutputStream, sourceLocation: String) {
        Log.d(tag, "Zipping files from '$sourceLocation'")
        createDirIfNotExists(sourceLocation)

        ZipOutputStream(outputStream).use { zipOut ->
            for (file in File(sourceLocation).listFiles()) {
                if (file.isDirectory) {
                    continue
                }

                zipOut.putNextEntry(ZipEntry(file.name))
                zipOut.write(file.readBytes())
                zipOut.closeEntry()
            }
        }
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