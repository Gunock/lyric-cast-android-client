/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.common.helpers

import android.util.Log
import net.lingala.zip4j.io.inputstream.ZipInputStream
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.LocalFileHeader
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File
import java.io.InputStream
import java.io.OutputStream


object FileHelper {
    private const val TAG = "FileHelper"

    fun unzip(inputStream: InputStream, targetLocation: String) {
        Log.d(TAG, "Unzipping stream to '$targetLocation'")

        createDirectory(targetLocation)
        ZipInputStream(inputStream).use { zipInputStream ->
            while (true) {
                val entry: LocalFileHeader = zipInputStream.nextEntry ?: break
                val filePath = "${targetLocation}/${entry.fileName}"

                if (entry.isDirectory) {
                    createDirectory(filePath)
                } else {
                    File(filePath).outputStream().use { it.write(zipInputStream.readBytes()) }
                    Log.v(TAG, "Unzipped file at $filePath")
                }
            }
        }
    }

    fun zip(outputStream: OutputStream, sourceLocation: String): Boolean {
        Log.d(TAG, "Zipping files from '$sourceLocation'")
        createDirectory(sourceLocation)

        val fileList = File(sourceLocation).listFiles() ?: return false

        val zipOut = ZipOutputStream(outputStream)
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
        return true
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