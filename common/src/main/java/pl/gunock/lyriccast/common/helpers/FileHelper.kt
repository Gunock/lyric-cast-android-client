/*
 * Created by Tomasz Kiljanczyk on 4/9/21 12:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 11:59 AM
 */

package pl.gunock.lyriccast.common.helpers

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import net.lingala.zip4j.io.inputstream.ZipInputStream
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File


object FileHelper {
    private const val TAG = "FileHelper"

    fun unzip(resolver: ContentResolver, sourceUri: Uri, targetLocation: String): Boolean {
        Log.d(TAG, "Unzipping stream to '$targetLocation'")

        createDirectory(targetLocation)
        val inputStream = resolver.openInputStream(sourceUri) ?: return false
        val zipInputStream = ZipInputStream(inputStream)

        while (true) {
            val entry = zipInputStream.nextEntry ?: break
            val filePath = "${targetLocation}/${entry.fileName}"

            if (entry.isDirectory) {
                createDirectory(filePath)
            } else {
                File(filePath).outputStream().use { it.write(zipInputStream.readBytes()) }
                Log.v(TAG, "Unzipped file at $filePath")
            }
        }
        zipInputStream.close()
        inputStream.close()
        return true
    }

    fun zip(resolver: ContentResolver, targetUri: Uri, sourceLocation: String) {
        Log.d(TAG, "Zipping files from '$sourceLocation'")
        createDirectory(sourceLocation)

        val fileList = File(sourceLocation).listFiles() ?: return

        val zipOut = ZipOutputStream(resolver.openOutputStream(targetUri))
        val parameters = ZipParameters()
        parameters.compressionMethod = CompressionMethod.DEFLATE
        parameters.compressionLevel = CompressionLevel.NORMAL
        for (file in fileList) {
            if (file.isDirectory) {
                zipDirectory(zipOut, file, parameters)
            } else {
                parameters.fileNameInZip = file.name
                zipOut.putNextEntry(parameters)
                zipOut.write(file.readBytes())
                zipOut.closeEntry()
            }
        }
        zipOut.close()
    }

    private fun zipDirectory(
        outputStream: ZipOutputStream,
        directory: File,
        parameters: ZipParameters,
        outputPath: String = directory.path
    ) {
        val fileList = directory.listFiles() ?: return
        for (file in fileList) {
            val outputFilePath = "${outputPath}/${file.name}"
            if (file.isDirectory) {
                zipDirectory(outputStream, file, parameters, outputFilePath)
                continue
            }

            parameters.fileNameInZip = outputFilePath
            outputStream.putNextEntry(parameters)
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
            if (File(path).mkdirs()) {
                Log.d(TAG, "'$path' directory creation succeeded")
            } else {
                Log.d(TAG, "'$path' directory creation failed")
            }
        } else {
            Log.d(TAG, "'$path' already exists")
        }
    }
}