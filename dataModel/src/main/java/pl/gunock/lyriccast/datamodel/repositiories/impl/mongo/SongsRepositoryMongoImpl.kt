/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.where
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository

internal class SongsRepositoryMongoImpl(
    private val mRealm: Realm = Realm.getInstance(
        RealmConfiguration.Builder()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
    )
) : SongsRepository {

    private val allSongs: RealmResults<SongDocument> =
        mRealm.where<SongDocument>().findAllAsync()

    override fun getAllSongs(): Flowable<List<Song>> {
        return allSongs.asFlowable()
            .map { songDocuments ->
                songDocuments.map { it.toGenericModel() }
            }
    }

    override fun getSong(id: String): Song? {
        val songDocument = mRealm.where<SongDocument>()
            .equalTo("id", ObjectId(id))
            .findFirst()

        return songDocument?.toGenericModel()
    }

    override fun upsertSong(song: Song) {
        val songDocument = SongDocument(song)
        mRealm.executeTransaction { mRealm.insertOrUpdate(songDocument) }
    }

    override fun deleteSongs(songIds: Collection<String>) {
        for (id in songIds) {
            allSongs.where()
                .equalTo("id", ObjectId(id))
                .findFirst()
                ?.deleteFromRealm()
        }
    }

}