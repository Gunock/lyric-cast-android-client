/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 14:52
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/12/2021, 14:24
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.impl.DataTransferRepositoryBaseImpl

internal class DataTransferRepositoryMongoImpl(
    private val dispatcher: CoroutineDispatcher
) : DataTransferRepositoryBaseImpl() {

    private val realm: Realm = runBlocking(dispatcher) {
        Realm.getInstance(RealmConfiguration.Builder().build())
    }

    override suspend fun clearDatabase() =
        withContext(dispatcher) {
            realm.executeTransaction { it.deleteAll() }
        }

    override suspend fun getAllSongs(): List<Song> =
        withContext(dispatcher) {
            realm.where<SongDocument>().findAllAsync()
                .map { it.toGenericModel() }
        }

    override suspend fun getAllSetlists(): List<Setlist> =
        withContext(dispatcher) {
            realm.where<SetlistDocument>().findAllAsync()
                .map { it.toGenericModel() }
        }

    override suspend fun getAllCategories(): List<Category> =
        withContext(dispatcher) {
            realm.where<CategoryDocument>().findAllAsync()
                .map { it.toGenericModel() }
        }

    override suspend fun upsertSongs(songs: Iterable<Song>) =
        withContext(dispatcher) {
            val songDocuments = songs.map { SongDocument(it) }
            realm.executeTransaction { it.insertOrUpdate(songDocuments) }
        }

    override suspend fun upsertSetlists(setlists: Iterable<Setlist>) =
        withContext(dispatcher) {
            val setlistDocuments = setlists.map { SetlistDocument(it) }
            realm.executeTransaction { it.insertOrUpdate(setlistDocuments) }
        }

    override suspend fun upsertCategories(categories: Iterable<Category>) =
        withContext(dispatcher) {
            val categoryDocuments = categories.map { CategoryDocument(it) }
            realm.executeTransaction { it.insertOrUpdate(categoryDocuments) }
        }

}