/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 10:03
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 09:54
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository

internal class SongsRepositoryMongoImpl : SongsRepository {

    private val realm: Realm = Realm.getInstance(RealmConfiguration.Builder().build())

    override fun getAllSongs(): Flowable<List<Song>> {
        return realm.where<SongDocument>().findAllAsync()
            .asFlowable()
            .map { songDocuments ->
                songDocuments.map { it.toGenericModel() }
            }
    }

    override fun getSong(id: String): Song? {
        val songDocument = realm.where<SongDocument>()
            .equalTo("id", ObjectId(id))
            .findFirst()

        return songDocument?.toGenericModel()
    }

    override suspend fun upsertSong(song: Song) {
        val songDocument = SongDocument(song)

        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            transactionRealm.insertOrUpdate(songDocument)
        }
    }

    override suspend fun deleteSongs(songIds: Collection<String>) {
        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
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