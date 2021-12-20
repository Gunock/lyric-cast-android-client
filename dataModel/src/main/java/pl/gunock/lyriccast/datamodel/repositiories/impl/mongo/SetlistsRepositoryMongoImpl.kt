/*
 * Created by Tomasz Kiljanczyk on 21/12/2021, 00:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 21/12/2021, 00:05
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository

internal class SetlistsRepositoryMongoImpl : SetlistsRepository {

    private val realm: Realm = Realm.getInstance(RealmConfiguration.Builder().build())

    override fun getAllSetlists(): Flowable<List<Setlist>> {
        return realm.where<SetlistDocument>().findAllAsync()
            .asFlowable()
            .map { setlistDocuments ->
                setlistDocuments.map { it.toGenericModel() }
            }
    }

    override fun getSetlist(id: String): Setlist? {
        val setlistDocument = realm.where<SetlistDocument>()
            .equalTo("id", ObjectId(id))
            .findFirst()

        return setlistDocument?.toGenericModel()
    }

    override suspend fun upsertSetlist(setlist: Setlist) {
        val setlistDocument = SetlistDocument(setlist)

        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            transactionRealm.insertOrUpdate(setlistDocument)
        }
    }

    override suspend fun deleteSetlists(setlistIds: Collection<String>) {
        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            for (id in setlistIds) {
                transactionRealm.where<SetlistDocument>().findAll()
                    .where()
                    .equalTo("id", ObjectId(id))
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

}