/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 22:34
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.where
import pl.gunock.lyriccast.datamodel.R
import pl.gunock.lyriccast.datamodel.extentions.toRealmList
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto

internal class DataTransferRepositoryMongoImpl(
    private val mResources: Resources,
    private val mRealm: Realm = Realm.getInstance(
        RealmConfiguration.Builder()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
    )
) : DataTransferRepository {

    private companion object {
        const val TAG = "DataTransferRepository"
    }

    override fun clearDatabase() {
        mRealm.executeTransaction { mRealm.deleteAll() }
    }

    override fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        mRealm.executeTransaction {
            executeDataImport(data, message, options)
        }
    }

    override fun importSongs(
        songDtoSet: Set<SongDto>,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        val categoryDtos: List<CategoryDto> = songDtoSet.map { songDto -> songDto.category }
            .distinct()
            .mapIndexedNotNull { index, categoryName ->
                CategoryDto(
                    name = categoryName?.take(30) ?: return@mapIndexedNotNull null,
                    color = options.colors[index % options.colors.size]
                )
            }

        val data = DatabaseTransferData(songDtoSet.toList(), categoryDtos, null)
        importSongs(data, message, options)
    }

    override fun getDatabaseTransferData(): DatabaseTransferData {
        val songs: RealmResults<SongDocument> = mRealm.where<SongDocument>().findAll()
        val categories: RealmResults<CategoryDocument> = mRealm.where<CategoryDocument>().findAll()
        val setlists: RealmResults<SetlistDocument> = mRealm.where<SetlistDocument>().findAll()

        val songDtos: List<SongDto> = songs.map { it.toDto() }
        val categoryDtos: List<CategoryDto> = categories.map { it.toDto() }
        val setlistDtos: List<SetlistDto> = setlists.map { it.toDto() }

        return DatabaseTransferData(
            songDtos = songDtos,
            categoryDtos = categoryDtos,
            setlistDtos = setlistDtos
        )
    }

    override fun close() {
        mRealm.close()
    }


    private fun executeDataImport(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        if (options.deleteAll) {
            mRealm.deleteAll()
        }

        val removeConflicts: Boolean = !options.deleteAll && !options.replaceOnConflict

        if (data.categoryDtos != null) {
            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_categories))
            executeCategoryImport(data.categoryDtos, options, removeConflicts)
        }

        if (data.songDtos != null) {
            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_songs))
            executeSongImport(data.songDtos, options, removeConflicts)
        }

        if (data.setlistDtos != null) {
            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_setlists))
            executeSetlistImport(data.setlistDtos, options, removeConflicts)
        }

        message.postValue(mResources.getString(R.string.data_transfer_processor_finishing_import))
        Log.d(TAG, "Finished import")
    }

    private fun executeCategoryImport(
        categoryDtos: List<CategoryDto>,
        options: ImportOptions,
        removeConflicts: Boolean
    ) {
        val categories = categoryDtos.map { CategoryDocument(it) }.toMutableList()

        val allCategories = mRealm.where<CategoryDocument>().findAll()

        val categoryNames = allCategories.map { it.name }.toSet()
        if (removeConflicts) {
            categories.removeAll { it.name in categoryNames }
        } else if (options.replaceOnConflict) {
            val categoryNameMap = allCategories.map { it.name to it.id }.toMap()
            val categoriesToAdd: MutableList<CategoryDocument> = mutableListOf()

            categories.removeAll {
                if (it.name in categoryNames) {
                    categoriesToAdd.add(CategoryDocument(it, categoryNameMap[it.name]!!))
                    return@removeAll true
                }
                return@removeAll false
            }
            categories.addAll(categoriesToAdd)
        }

        mRealm.insertOrUpdate(categories)
    }

    private fun executeSongImport(
        songDtos: List<SongDto>,
        options: ImportOptions,
        removeConflicts: Boolean
    ) {
        val categoryMap: Map<String, CategoryDocument> = mRealm.where<CategoryDocument>()
            .findAll()
            .map { it.name to it }
            .toMap()

        val songs: MutableList<SongDocument> = songDtos
            .map { dto ->
                val lyricsSections: List<LyricsSectionDocument> = dto.lyrics
                    .map { LyricsSectionDocument(name = it.key, text = it.value) }

                val song = SongDocument(dto, categoryMap[dto.category])
                song.lyrics = lyricsSections.toRealmList()
                song.presentation = dto.presentation.toRealmList()

                return@map song
            }.toMutableList()


        val allSongs = mRealm.where<SongDocument>().findAll()
        val songTitles = allSongs.map { it.title }.toSet()

        if (removeConflicts) {
            songs.removeAll { it.title in songTitles }
        } else if (options.replaceOnConflict) {
            val songTitleMap = allSongs.map { it.title to it.id }.toMap()
            val songsToAdd: MutableList<SongDocument> = mutableListOf()
            songs.removeAll {
                if (it.title in songTitles) {
                    songsToAdd.add(SongDocument(it, songTitleMap[it.title]!!))
                    return@removeAll true
                }
                return@removeAll false
            }
            songs.addAll(songsToAdd)
        }

        mRealm.insertOrUpdate(songs)
    }

    private fun executeSetlistImport(
        setlistDtos: List<SetlistDto>,
        options: ImportOptions,
        removeConflicts: Boolean
    ) {
        val setlists: MutableList<SetlistDocument> = setlistDtos
            .map { SetlistDocument(it) }
            .toMutableList()

        val allSetlists: List<SetlistDocument> = mRealm.where<SetlistDocument>().findAll()
        val setlistNames = allSetlists.map { it.name }.toSet()

        if (removeConflicts) {
            setlists.removeAll { it.name in setlistNames }
        } else if (options.replaceOnConflict) {
            val setlistNameMap = allSetlists.map { it.name to it.id }.toMap()

            val setlistsToAdd: MutableList<SetlistDocument> = mutableListOf()
            setlists.removeAll {
                if (it.name in setlistNames) {
                    setlistsToAdd.add(SetlistDocument(it, setlistNameMap[it.name]!!))
                    return@removeAll true
                }
                return@removeAll false
            }
            setlists.addAll(setlistsToAdd)
        }

        val songTitleMap: Map<String, SongDocument> = mRealm.where<SongDocument>()
            .findAll()
            .map { it.title to it }
            .toMap()
        val setlistDtoNameMap = setlistDtos.map { it.name to it.songs }.toMap()
        setlists.forEach { setlist ->
            val presentation: List<SongDocument> = setlistDtoNameMap[setlist.name]!!
                .map { songTitleMap[it]!! }

            setlist.presentation = presentation.toRealmList()
        }

        mRealm.insertOrUpdate(setlists)
    }

}