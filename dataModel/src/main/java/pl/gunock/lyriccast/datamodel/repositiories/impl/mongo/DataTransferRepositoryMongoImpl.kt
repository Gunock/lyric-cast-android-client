/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 17:30
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 15:50
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.impl.DataTransferRepositoryBaseImpl

internal class DataTransferRepositoryMongoImpl(private val realm: Realm) :
    DataTransferRepositoryBaseImpl() {

    override suspend fun clearDatabase() = realm.write { deleteAll() }

    override suspend fun getAllSongs(): List<Song> =
        withContext(Dispatchers.IO) {
            realm.query<SongDocument>().find()
                .map { it.toGenericModel() }
        }

    override suspend fun getAllSetlists(): List<Setlist> =
        withContext(Dispatchers.IO) {
            realm.query<SetlistDocument>().find()
                .map { it.toGenericModel() }
        }

    override suspend fun getAllCategories(): List<Category> =
        withContext(Dispatchers.IO) {
            realm.query<CategoryDocument>().find()
                .map { it.toGenericModel() }
        }

    override suspend fun upsertSongs(songs: Iterable<Song>) =
        realm.write {
            songs.map { SongDocument(it) }
                .forEach { copyToRealm(it, UpdatePolicy.ALL) }
        }

    override suspend fun upsertSetlists(setlists: Iterable<Setlist>) =
        realm.write {
            setlists.map { SetlistDocument(it) }
                .forEach { copyToRealm(it, UpdatePolicy.ALL) }
        }

    override suspend fun upsertCategories(categories: Iterable<Category>) =
        realm.write {
            categories.map { CategoryDocument(it) }
                .forEach { copyToRealm(it, UpdatePolicy.ALL) }
        }

}