/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 5:11 PM
 */

package pl.gunock.lyriccast.datamodel

import android.content.res.Resources
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import pl.gunock.lyriccast.datamodel.entities.CategoryDocument
import pl.gunock.lyriccast.datamodel.entities.SetlistDocument
import pl.gunock.lyriccast.datamodel.entities.SongDocument
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto

internal class MongoDataTransferProcessor(
    private val mResources: Resources,
    private val mRealm: Realm
) {
    private companion object {
        const val TAG = "DataTransferProcessor"
    }

    // TODO: Rework for MongoDB
//    suspend fun importSongs(
//        data: DatabaseTransferData,
//        message: MutableLiveData<String>,
//        options: ImportOptions
//    ) {
//        if (options.deleteAll) {
//            mRepository.clear()
//        }
//
//        val removeConflicts = !options.deleteAll && !options.replaceOnConflict
//
//        if (data.categoryDtos != null) {
//            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_categories))
//            val categories = data.categoryDtos.map { Category(it) }.toMutableList()
//            val allCategories = mRepository.getAllCategories()
//            val categoryNames = allCategories.map { it.name }.toSet()
//            if (removeConflicts) {
//                categories.removeAll { it.name in categoryNames }
//            } else if (options.replaceOnConflict) {
//                val categoryNameMap = allCategories.map { it.name to it }.toMap()
//                val categoriesToAdd: MutableList<Category> = mutableListOf()
//                categories.removeAll {
//                    if (it.name in categoryNames) {
//                        categoriesToAdd.add(it.copy(categoryId = categoryNameMap[it.name]!!.categoryId))
//                        return@removeAll true
//                    }
//                    return@removeAll false
//                }
//                categories.addAll(categoriesToAdd)
//            }
//
//            mRepository.upsertCategories(categories)
//        }
//
//        if (data.songDtos != null) {
//            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_songs))
//
//            val categoryMap: Map<String, Category> = mRepository.getAllCategories()
//                .map { it.name to it }
//                .toMap()
//
//            var orderMap: MutableMap<String, List<Pair<String, Int>>> = mutableMapOf()
//            val songsWithLyricsSections: MutableList<SongWithLyricsSections> = data.songDtos
//                .map { songDto ->
//                    val song = Song(songDto, categoryMap[songDto.category]?.categoryId)
//                    val lyricsSections: List<LyricsSection> = songDto.lyrics
//                        .map { LyricsSection(songId = -1, name = it.key, text = it.value) }
//
//                    val order: List<Pair<String, Int>> = songDto.presentation
//                        .mapIndexed { index, sectionName -> sectionName to index }
//
//                    orderMap[song.title] = order
//                    return@map SongWithLyricsSections(song, lyricsSections)
//                }.toMutableList()
//
//            val allSongs = mRepository.getAllSongs()
//            val songTitles = allSongs.map { it.title }.toSet()
//            if (removeConflicts) {
//                orderMap = orderMap.filter { it.key !in songTitles }.toMutableMap()
//                songsWithLyricsSections.removeAll { it.song.title in songTitles }
//            } else if (options.replaceOnConflict) {
//                val songTitleMap = allSongs.map { it.title to it }.toMap()
//                val songsToAdd: MutableList<SongWithLyricsSections> = mutableListOf()
//                songsWithLyricsSections.removeAll {
//                    if (it.song.title in songTitles) {
//                        songsToAdd
//                            .add(it.copy(song = it.song.copy(songId = songTitleMap[it.song.title]!!.songId)))
//                        return@removeAll true
//                    }
//                    return@removeAll false
//                }
//                songsWithLyricsSections.addAll(songsToAdd)
//            }
//
//            mRepository.upsertSongs(songsWithLyricsSections, orderMap)
//        }
//
//        if (data.setlistDtos != null) {
//            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_setlists))
//
//            val songTitleMap: Map<String, Song> = mRepository.getAllSongs()
//                .map { it.title to it }
//                .toMap()
//
//            val setlists: MutableList<Setlist> = data.setlistDtos
//                .map { Setlist(it) }
//                .toMutableList()
//
//            val allSetlists: List<SetlistWithSongs> = mRepository.getAllSetlists()
//            val setlistNames = allSetlists.map { it.setlist.name }.toSet()
//            if (removeConflicts) {
//
//                setlists.removeAll { it.name in setlistNames }
//            } else if (options.replaceOnConflict) {
//                val setlistNameMap = allSetlists.map { it.setlist.name to it.setlist }.toMap()
//                val setlistsToAdd: MutableList<Setlist> = mutableListOf()
//                setlists.removeAll {
//                    if (it.name in setlistNames) {
//                        setlistsToAdd.add(it.copy(setlistId = setlistNameMap[it.name]!!.setlistId))
//                        return@removeAll true
//                    }
//                    return@removeAll false
//                }
//                setlists.addAll(setlistsToAdd)
//            }
//
//            val newSetlistNames = setlists.map { it.name }.toSet()
//            val setlistCrossRefMap: Map<String, List<SetlistSongCrossRef>> =
//                data.setlistDtos
//                    .filter { it.name in newSetlistNames }
//                    .map { setlistDto ->
//                        val songList = setlistDto.songs
//
//                        val setlistSongCrossRefs: List<SetlistSongCrossRef> =
//                            songList.mapIndexed { index, songTitle ->
//                                SetlistSongCrossRef(null, -1, songTitleMap[songTitle]!!.id, index)
//                            }
//                        return@map setlistDto.name to setlistSongCrossRefs
//                    }.toMap()
//
//            mRepository.upsertSetlists(setlists, setlistCrossRefMap)
//        }
//
//        message.postValue(mResources.getString(R.string.data_transfer_processor_finishing_import))
//        Log.d(TAG, "Finished import")
//    }

    fun getDatabaseTransferData(): DatabaseTransferData {
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
}