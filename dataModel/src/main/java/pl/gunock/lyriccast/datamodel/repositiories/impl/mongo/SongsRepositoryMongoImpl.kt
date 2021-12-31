/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:07
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository

internal class SongsRepositoryMongoImpl(
    private val dispatcher: CoroutineDispatcher
) : SongsRepository {

    private val realm: Realm = runBlocking(dispatcher) {
        Realm.getInstance(RealmConfiguration.Builder().build())
    }

    override fun getAllSongs(): Flow<List<Song>> =
        runBlocking(dispatcher) {
            realm.where<SongDocument>()
                .findAllAsync()
                .toFlow()
                .map { songs -> songs.map { it.toGenericModel() } }
                .flowOn(dispatcher)

        }

    override fun getSong(id: String): Song? =
        runBlocking(dispatcher) {
            val songDocument = realm.where<SongDocument>()
                .equalTo("id", ObjectId(id))
                .findFirst()

            songDocument?.toGenericModel()
        }

    override suspend fun upsertSong(song: Song) =
        withContext(dispatcher) {
            val songDocument = SongDocument(song)
            realm.executeTransaction { it.insertOrUpdate(songDocument) }
        }

    override suspend fun deleteSongs(songIds: Collection<String>) =
        withContext(dispatcher) {
            realm.executeTransaction { transactionRealm ->
                for (id in songIds) {
                    transactionRealm.where<SongDocument>().findAll()
                        .where()
                        .equalTo("id", ObjectId(id))
                        .findFirst()
                        ?.deleteFromRealm()
                }
            }
        }

}