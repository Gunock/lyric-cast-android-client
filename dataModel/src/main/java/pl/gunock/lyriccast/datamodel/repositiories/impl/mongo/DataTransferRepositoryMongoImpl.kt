/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:07
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.impl.DataTransferRepositoryBaseImpl

internal class DataTransferRepositoryMongoImpl : DataTransferRepositoryBaseImpl() {

    private val realm: Realm = Realm.getInstance(RealmConfiguration.Builder().build())

    override suspend fun clearDatabase() {
        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            transactionRealm.deleteAll()
        }
    }

    override fun getAllSongs(): List<Song> {
        return realm.where<SongDocument>()
            .findAll()
            .map { it.toGenericModel() }
    }

    override fun getAllSetlists(): List<Setlist> {
        return realm.where<SetlistDocument>()
            .findAll()
            .map { it.toGenericModel() }
    }

    override fun getAllCategories(): List<Category> {
        return realm.where<CategoryDocument>()
            .findAll()
            .map { it.toGenericModel() }
    }

    override suspend fun upsertSongs(songs: Iterable<Song>) {
        val songDocuments = songs.map { SongDocument(it) }
        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            transactionRealm.insertOrUpdate(songDocuments)
        }
    }

    override suspend fun upsertSetlists(setlists: Iterable<Setlist>) {
        val setlistDocuments = setlists.map { SetlistDocument(it) }
        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            transactionRealm.insertOrUpdate(setlistDocuments)
        }
    }

    override suspend fun upsertCategories(categories: Iterable<Category>) {
        val categoryDocuments = categories.map { CategoryDocument(it) }
        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            transactionRealm.insertOrUpdate(categoryDocuments)
        }
    }

}