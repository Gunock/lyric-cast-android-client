/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 5:11 PM
 */

package pl.gunock.lyriccast.datamodel

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.where
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.entities.CategoryDocument
import pl.gunock.lyriccast.datamodel.entities.SetlistDocument
import pl.gunock.lyriccast.datamodel.entities.SongDocument
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.Closeable


class MongoDatabaseViewModel(
    resources: Resources,
    private val mRealmConfig: RealmConfiguration = RealmConfiguration.Builder()
        .allowQueriesOnUiThread(true)
        .allowWritesOnUiThread(true)
        .build(),
    private val mRealm: Realm = Realm.getInstance(mRealmConfig)
) : ViewModel(), Closeable {

    private val mDataTransferProcessor = MongoDataTransferProcessor(resources, mRealm)

    val allSongs: RealmResults<SongDocument> =
        mRealm.where<SongDocument>().findAllAsync()

    val allSetlists: RealmResults<SetlistDocument> =
        mRealm.where<SetlistDocument>().findAllAsync()

    val allCategories: RealmResults<CategoryDocument> =
        mRealm.where<CategoryDocument>().findAllAsync()

    fun upsertSong(song: SongDocument) = mRealm.executeTransaction { mRealm.insertOrUpdate(song) }

    fun deleteSongs(songIds: Collection<ObjectId>) = mRealm.executeTransaction {
        for (id in songIds) {
            allSongs.where()
                .equalTo("id", id)
                .findFirst()
                ?.deleteFromRealm()
        }
    }

    fun upsertSetlist(setlist: SetlistDocument) =
        mRealm.executeTransaction { mRealm.insertOrUpdate(setlist) }


//    fun deleteSetlists(setlistIds: List<Long>) =
//        viewModelScope.launch { mRepository.deleteSetlists(setlistIds) }

    fun upsertCategory(category: CategoryDocument) =
        mRealm.executeTransaction { mRealm.insertOrUpdate(category) }

    fun deleteCategories(categoryIds: Collection<ObjectId>) = mRealm.executeTransaction {
        for (id in categoryIds) {
            allCategories.where()
                .equalTo("id", id)
                .findFirst()
                ?.deleteFromRealm()
        }
    }


    // TODO: Implement
    suspend fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
//        mDataTransferProcessor.importSongs(data, message, options)
    }

    // TODO: Implement
    suspend fun importSongs(
        songDtoSet: Set<SongDto>,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
//        val categoryDtos: List<CategoryDto> = songDtoSet.map { songDto -> songDto.category }
//            .distinct()
//            .mapIndexedNotNull { index, categoryName ->
//                CategoryDto(
//                    name = categoryName?.take(30) ?: return@mapIndexedNotNull null,
//                    color = options.colors[index % options.colors.size]
//                )
//            }
//
//        val data = DatabaseTransferData(songDtoSet.toList(), categoryDtos, null)
//        mDataTransferProcessor.importSongs(data, message, options)
    }

    fun getDatabaseTransferData(): DatabaseTransferData {
        return mDataTransferProcessor.getDatabaseTransferData()
    }

    override fun close() {
        mRealm.close()
    }

    class Factory(
        private val mResources: Resources
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(MongoDatabaseViewModel::class.java)) {
                throw IllegalArgumentException("Unknown ViewModel class")
            }

            @Suppress("UNCHECKED_CAST")
            return MongoDatabaseViewModel(mResources) as T
        }
    }
}

