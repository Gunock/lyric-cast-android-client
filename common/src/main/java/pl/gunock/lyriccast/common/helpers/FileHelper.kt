/*
 * Created by Tomasz Kiljanczyk on 4/4/21 12:28 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 12:25 AM
 */

package pl.gunock.lyriccast.common.helpers

import android.content.ContentResolver
import android.os.Build
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

        val zipIn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ZipInputStream(inputStream, Charsets.ISO_8859_1)
        } else {
            ZipInputStream(inputStream)
        }

        while (true) {
            val zipEntry: ZipEntry = zipIn.nextEntry ?: break

            Log.d(TAG, "Extracting file: ${zipEntry.name}")


            if (zipEntry.isDirectory) {
                continue
            }

            val file = File("$targetLocation/${zipEntry.name}")
            val fileUri = file.toUri()
            Log.d(TAG, "Extracting file to: ${file.canonicalPath}")

            createDirectory(file.parent)
            resolver.openOutputStream(fileUri)!!.write(zipIn.readBytes())
            zipIn.closeEntry()
            Log.d(TAG, "File extracted to: $fileUri")
        }
        zipIn.close()
    }

    fun zip(outputStream: OutputStream, sourceLocation: String) {
        Log.d(TAG, "Zipping files from '$sourceLocation'")
        createDirectory(sourceLocation)

        val fileList = File(sourceLocation).listFiles() ?: return

        val zipOut = ZipOutputStream(outputStream)
        for (file in fileList) {
            if (file.isDirectory) {
                zipDirectory(zipOut, file, file.name)
                continue
            }

            zipOut.putNextEntry(ZipEntry(file.name))
            zipOut.write(file.readBytes())
            zipOut.closeEntry()
        }
        zipOut.close()
    }

    private fun zipDirectory(
        outputStream: ZipOutputStream,
        directory: File,
        directoryPath: String
    ) {
        val fileList = directory.listFiles() ?: return
        for (file in fileList) {
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

    private fun createDirectory(path: String?) {
        if (path.isNullOrBlank()) {
            return
        }

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