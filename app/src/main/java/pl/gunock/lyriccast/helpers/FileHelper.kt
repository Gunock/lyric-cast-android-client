/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:03 PM
 */

package pl.gunock.lyriccast.helpers

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
        createDirectory(targetLocation)

        val zipIn = ZipInputStream(inputStream)

        var zipEntryInput: ZipEntry?
        while (zipIn.nextEntry.also { zipEntryInput = it } != null) {
            val zipEntry = zipEntryInput!!

            if (zipEntry.isDirectory) {
                continue
            }

            Log.d(TAG, "Extracting file: ${zipEntry.name}")

            val file = File("$targetLocation/${zipEntry.name}")
            val fileUri = file.toUri()
            Log.d(TAG, "Extracting file to: $fileUri")

            createDirectory(file.parent?.toUri().toString())
            with(resolver.openOutputStream(fileUri)) {
                this!!.write(zipIn.readBytes())
                zipIn.closeEntry()
            }
            Log.d(TAG, "File extracted to: $fileUri")
        }
        zipIn.close()
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun zip(outputStream: OutputStream, sourceLocation: String) {
        Log.d(TAG, "Zipping files from '$sourceLocation'")
        createDirectory(sourceLocation)

        with(ZipOutputStream(outputStream)) {
            for (file in File(sourceLocation).listFiles()) {
                if (file.isDirectory) {
                    zipDirectory(this, file, file.name)
                    continue
                }

                putNextEntry(ZipEntry(file.name))
                write(file.readBytes())
                closeEntry()
            }
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun zipDirectory(
        outputStream: ZipOutputStream,
        directory: File,
        directoryPath: String
    ) {
        for (file in directory.listFiles()) {
            val outputFilePath = "$directoryPath/${file.name}"
            if (file.isDirectory) {
                zipDirectory(outputStream, file, outputFilePath)
                continue
            }

            outputStream.putNextEntry(ZipEntry(outputFilePath))
            outputStream.write(file.readBytes())
            outputStream.closeEntry()
        }
    }

    private fun createDirectory(path: String) {
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