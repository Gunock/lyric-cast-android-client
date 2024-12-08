/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.common.helpers.FileHelper
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.DatabaseTransferData
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.ImportOptions
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datatransfer.enums.SongXmlParserType
import dev.thomas_kiljanczyk.lyriccast.datatransfer.extensions.toJSONObjectList
import dev.thomas_kiljanczyk.lyriccast.datatransfer.factories.ImportSongXmlParserFactory
import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.CategoryDto
import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SetlistDto
import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SongDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class MainModel @Inject constructor(
    private val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG = "MainViewModel"
    }

    fun exportAll(
        cacheDir: String,
        outputStream: OutputStream,
    ): Flow<Int> = flow {
        val exportData = dataTransferRepository.getDatabaseTransferData()

        val exportDir = File(cacheDir, ".export")
        exportDir.deleteRecursively()
        exportDir.mkdirs()

        emit(R.string.main_activity_export_saving_json)
        val songsString = JSONArray(exportData.songDtos!!.map { it.toJson() }).toString()
        val categoriesString =
            JSONArray(exportData.categoryDtos!!.map { it.toJson() }).toString()
        val setlistsString =
            JSONArray(exportData.setlistDtos!!.map { it.toJson() }).toString()

        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)
        File(exportDir, "setlists.json").writeText(setlistsString)

        emit(R.string.main_activity_export_saving_zip)
        FileHelper.zip(outputStream, exportDir.path)

        emit(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
    }.flowOn(Dispatchers.Default)

    suspend fun importLyricCast(
        cacheDir: String,
        inputStream: InputStream,
        importOptions: ImportOptions
    ): Flow<Int>? {
        val transferData: DatabaseTransferData = getImportData(cacheDir, inputStream) ?: return null

        return dataTransferRepository.importData(transferData, importOptions)
    }

    suspend fun importOpenSong(
        cacheDir: String,
        inputStream: InputStream,
        importOptions: ImportOptions
    ): Flow<Int>? {
        val importDir = File(cacheDir)
        val importSongXmlParser =
            ImportSongXmlParserFactory.create(importDir, SongXmlParserType.OPEN_SONG)

        val importedSongs: Set<SongDto> = try {
            importSongXmlParser.parseZip(inputStream)
        } catch (exception: Exception) {
            Log.w(TAG, exception)
            null
        } ?: return null

        return dataTransferRepository.importSongs(importedSongs, importOptions)
    }

    private fun getImportData(
        cacheDir: String,
        inputStream: InputStream
    ): DatabaseTransferData? {
        val importDir = File(cacheDir, ".import")
        importDir.deleteRecursively()
        importDir.mkdirs()

        FileHelper.unzip(inputStream, importDir.path)

        try {
            val songsJson = JSONArray(File(importDir, "songs.json").readText())
            val categoriesJson = JSONArray(File(importDir, "categories.json").readText())

            val setlistsFile = File(importDir, "setlists.json")
            val setlistsJson: JSONArray? = if (setlistsFile.exists()) {
                JSONArray(File(importDir, "setlists.json").readText())
            } else {
                null
            }

            return DatabaseTransferData(
                songDtos = songsJson.toJSONObjectList().map { SongDto(it) },
                categoryDtos = categoriesJson.toJSONObjectList().map { CategoryDto(it) },
                setlistDtos = setlistsJson?.toJSONObjectList()?.map { SetlistDto(it) }
            )
        } catch (exception: Exception) {
            Log.e(TAG, exception.stackTraceToString())
            return null
        } finally {
            importDir.deleteRecursively()
        }
    }
}