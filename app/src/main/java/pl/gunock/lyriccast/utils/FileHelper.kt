/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 1:04 PM
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
    private const val TAG = "FileHelper"

    fun unzip(resolver: ContentResolver, inputStream: InputStream, targetLocation: String) {
        Log.d(TAG, "Unzipping stream to '$targetLocation'")
        createDirIfNotExists(targetLocation)

        val zipIn = ZipInputStream(inputStream)

        var zipEntryInput: ZipEntry?
        while (zipIn.nextEntry.also { zipEntryInput = it } != null) {
            val zipEntry = zipEntryInput!!

            if (zipEntry.isDirectory) {
                continue
            }

            Log.d(TAG, "Extracting file: ${zipEntry.name}")

            val filename: String = zipEntry.name.split("/").last()
            Log.d(TAG, "Extracting file to: ${targetLocation + filename}")

            val fileUri = File(targetLocation + filename).toUri()
            with(resolver.openOutputStream(fileUri)) {
                this!!.write(zipIn.readBytes())
                zipIn.closeEntry()
            }
            Log.d(TAG, "File extracted to: ${targetLocation + filename}")
        }
        zipIn.close()
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun zip(outputStream: OutputStream, sourceLocation: String) {
        Log.d(TAG, "Zipping files from '$sourceLocation'")
        createDirIfNotExists(sourceLocation)

        with(ZipOutputStream(outputStream)) {
            for (file in File(sourceLocation).listFiles()) {
                if (file.isDirectory) {
                    continue
                }

                putNextEntry(ZipEntry(file.name))
                write(file.readBytes())
                closeEntry()
            }
        }
    }

    private fun createDirIfNotExists(path: String) {
        val file = File(path)
        if (!file.exists()) {
            Log.d(TAG, "'$path' does not exists")
            if (File(path).mkdirs()) {
                Log.d(TAG, "'$path' directory creation succeeded")
            } else {
                Log.d(TAG, "'$path' directory creation failed")
            }
        }
    }
}