/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 14:52
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/12/2021, 14:24
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository

internal class SetlistsRepositoryMongoImpl(
    private val dispatcher: CoroutineDispatcher
) : SetlistsRepository {

    private val realm: Realm = runBlocking(dispatcher) {
        Realm.getInstance(RealmConfiguration.Builder().build())
    }

    override fun getAllSetlists(): Flowable<List<Setlist>> =
        runBlocking(dispatcher) {
            realm.where<SetlistDocument>().findAllAsync()
                .asFlowable()
                .map { setlistDocuments ->
                    setlistDocuments.map { it.toGenericModel() }
                }
        }

    override fun getSetlist(id: String): Setlist? =
        runBlocking(dispatcher) {
            val setlistDocument = realm.where<SetlistDocument>()
                .equalTo("id", ObjectId(id))
                .findFirst()

            setlistDocument?.toGenericModel()
        }

    override suspend fun upsertSetlist(setlist: Setlist) =
        withContext(dispatcher) {
            val setlistDocument = SetlistDocument(setlist)

            realm.executeTransaction { it.insertOrUpdate(setlistDocument) }
        }

    override suspend fun deleteSetlists(setlistIds: Collection<String>) =
        withContext(dispatcher) {
            realm.executeTransaction { transactionRealm ->
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