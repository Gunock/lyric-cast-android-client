/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:07
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository

internal class SongsRepositoryMongoImpl(private val realm: Realm) : SongsRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return realm.query<SongDocument>().find()
            .asFlow()
            .map { resultsChange -> resultsChange.list.map { it.toGenericModel() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getSong(id: String): Song? {
        val objectId = ObjectId(id)
        val songDocument = realm.query<SongDocument>("_id == $0", objectId).first().find()
        return songDocument?.toGenericModel()
    }

    override suspend fun upsertSong(song: Song): Unit =
        realm.write {
            val songDocument = SongDocument(song)
            copyToRealm(songDocument, UpdatePolicy.ALL)
        }

    override suspend fun deleteSongs(songIds: Collection<String>) =
        realm.write {
            songIds.map { ObjectId(it) }
                .mapNotNull { query<SongDocument>("_id == $0", it).first().find() }
                .forEach { delete(it) }
        }

}