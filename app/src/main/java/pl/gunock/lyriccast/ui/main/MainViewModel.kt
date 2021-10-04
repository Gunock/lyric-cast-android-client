/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:29
 */

package pl.gunock.lyriccast.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datatransfer.enums.SongXmlParserType
import pl.gunock.lyriccast.datatransfer.extensions.toJSONObjectList
import pl.gunock.lyriccast.datatransfer.factories.ImportSongXmlParserFactory
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG = "MainViewModel"
    }

    suspend fun exportAll(
        cacheDir: String,
        outputStream: OutputStream,
        messageResourceId: MutableLiveData<Int>
    ) {
        val exportData = withContext(Dispatchers.Main) {
            dataTransferRepository.getDatabaseTransferData()
        }

        withContext(Dispatchers.IO) {
            val exportDir = File(cacheDir, ".export")
            exportDir.deleteRecursively()
            exportDir.mkdirs()

            messageResourceId.postValue(R.string.main_activity_export_saving_json)
            val songsString = JSONArray(exportData.songDtos!!.map { it.toJson() }).toString()
            val categoriesString =
                JSONArray(exportData.categoryDtos!!.map { it.toJson() }).toString()
            val setlistsString =
                JSONArray(exportData.setlistDtos!!.map { it.toJson() }).toString()

            File(exportDir, "songs.json").writeText(songsString)
            File(exportDir, "categories.json").writeText(categoriesString)
            File(exportDir, "setlists.json").writeText(setlistsString)

            messageResourceId.postValue(R.string.main_activity_export_saving_zip)
            FileHelper.zip(outputStream, exportDir.path)

            messageResourceId.postValue(R.string.main_activity_export_deleting_temp)
            exportDir.deleteRecursively()
        }
    }

    suspend fun importLyricCast(
        cacheDir: String,
        inputStream: InputStream,
        messageResourceId: MutableLiveData<Int>,
        importError: MutableLiveData<Boolean>,
        importOptions: ImportOptions
    ): Boolean {
        val transferData: DatabaseTransferData? = withContext(Dispatchers.IO) {
            getImportData(cacheDir, inputStream)
        }

        if (transferData == null) {
            messageResourceId.postValue(R.string.main_activity_import_incorrect_file_format)
            importError.postValue(true)
            return false
        }

        withContext(Dispatchers.Main) {
            dataTransferRepository.importSongs(
                transferData,
                messageResourceId,
                importOptions
            )
        }

        return true
    }

    suspend fun importOpenSong(
        cacheDir: String,
        inputStream: InputStream,
        messageResourceId: MutableLiveData<Int>,
        importError: MutableLiveData<Boolean>,
        importOptions: ImportOptions
    ): Boolean {
        val importedSongs: Set<SongDto>? = withContext(Dispatchers.IO) {
            val importDir = File(cacheDir)
            val importSongXmlParser =
                ImportSongXmlParserFactory.create(importDir, SongXmlParserType.OPEN_SONG)

            return@withContext try {
                importSongXmlParser.parseZip(inputStream)
            } catch (exception: Exception) {
                Log.e(TAG, exception.stackTraceToString())
                null
            }
        }

        if (importedSongs == null) {
            messageResourceId.postValue(R.string.main_activity_import_incorrect_file_format)
            importError.postValue(true)
            return false
        }

        withContext(Dispatchers.Main) {
            dataTransferRepository.importSongs(
                importedSongs,
                messageResourceId,
                importOptions
            )
        }

        return true
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