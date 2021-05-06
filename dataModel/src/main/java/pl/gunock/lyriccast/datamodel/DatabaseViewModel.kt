 /*
 * Created by Tomasz Kiljanczyk on 06/05/2021, 13:42
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/05/2021, 23:56
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
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.datamodel.documents.SetlistDocument
import pl.gunock.lyriccast.datamodel.documents.SongDocument
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.Closeable


class DatabaseViewModel(
    resources: Resources,
    private val mRealm: Realm = Realm.getInstance(
        RealmConfiguration.Builder()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
    )
) : ViewModel(), Closeable {

    private val mDataTransferProcessor = DataTransferProcessor(resources, mRealm)

    val allSongs: RealmResults<SongDocument> =
        mRealm.where<SongDocument>().findAllAsync()

    val allSetlists: RealmResults<SetlistDocument> =
        mRealm.where<SetlistDocument>().findAllAsync()

    val allCategories: RealmResults<CategoryDocument> =
        mRealm.where<CategoryDocument>().findAllAsync()

    fun clearDatabase() {
        mRealm.executeTransaction { mRealm.deleteAll() }
    }

    fun getSong(id: ObjectId): SongDocument? {
        return mRealm.where<SongDocument>()
            .equalTo("id", id)
            .findFirst()
    }

    fun upsertSong(song: SongDocument) = mRealm.executeTransaction { mRealm.insertOrUpdate(song) }

    fun deleteSongs(songIds: Collection<ObjectId>) = mRealm.executeTransaction {
        for (id in songIds) {
            allSongs.where()
                .equalTo("id", id)
                .findFirst()
                ?.deleteFromRealm()
        }
    }

    fun getSetlist(id: ObjectId): SetlistDocument? {
        return mRealm.where<SetlistDocument>()
            .equalTo("id", id)
            .findFirst()
    }

    fun upsertSetlist(setlist: SetlistDocument) =
        mRealm.executeTransaction { mRealm.insertOrUpdate(setlist) }

    fun deleteSetlists(setlistIds: Collection<ObjectId>) = mRealm.executeTransaction {
        for (id in setlistIds) {
            allSetlists.where()
                .equalTo("id", id)
                .findFirst()
                ?.deleteFromRealm()
        }
    }

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

    fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        mRealm.executeTransaction {
            mDataTransferProcessor.importSongs(data, message, options)
        }
    }

    fun importSongs(
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
            if (!modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
                throw IllegalArgumentException("Unknown ViewModel class")
            }

            @Suppress("UNCHECKED_CAST")
            return DatabaseViewModel(mResources) as T
        }

        fun create(): DatabaseViewModel {
            return create(DatabaseViewModel::class.java)
        }
    }
}

