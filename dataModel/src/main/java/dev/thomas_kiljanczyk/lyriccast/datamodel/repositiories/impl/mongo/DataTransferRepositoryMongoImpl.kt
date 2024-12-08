/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.CategoryDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.SetlistDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.SongDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.DataTransferRepositoryBaseImpl
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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