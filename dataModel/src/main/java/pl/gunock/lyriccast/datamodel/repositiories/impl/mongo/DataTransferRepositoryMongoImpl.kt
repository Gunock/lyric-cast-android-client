/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 20:13
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.impl.DataTransferRepositoryBaseImpl

internal class DataTransferRepositoryMongoImpl(
    private val mRealm: Realm = Realm.getInstance(
        RealmConfiguration.Builder()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
    )
) : DataTransferRepositoryBaseImpl() {

    override fun executeTransaction(transaction: () -> Unit) {
        mRealm.executeTransaction {
            transaction()
        }
    }

    override fun clearDatabase() {
        mRealm.executeTransaction { mRealm.deleteAll() }

    }

    override fun getAllSongs(): List<Song> {
        return mRealm.where<SongDocument>()
            .findAll()
            .map { it.toGenericModel() }
    }

    override fun getAllSetlists(): List<Setlist> {
        return mRealm.where<SetlistDocument>()
            .findAll()
            .map { it.toGenericModel() }
    }

    override fun getAllCategories(): List<Category> {
        return mRealm.where<CategoryDocument>()
            .findAll()
            .map { it.toGenericModel() }
    }

    override fun upsertSongs(songs: Iterable<Song>) {
        val songDocuments = songs.map { SongDocument(it) }
        mRealm.executeTransaction { mRealm.insertOrUpdate(songDocuments) }
    }

    override fun upsertSetlists(setlists: Iterable<Setlist>) {
        val setlistDocuments = setlists.map { SetlistDocument(it) }
        mRealm.executeTransaction { mRealm.insertOrUpdate(setlistDocuments) }
    }

    override fun upsertCategories(categories: Iterable<Category>) {
        val categoryDocuments = categories.map { CategoryDocument(it) }
        mRealm.executeTransaction { mRealm.insertOrUpdate(categoryDocuments) }
    }

}