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
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository

internal class SetlistsRepositoryMongoImpl(private val realm: Realm) : SetlistsRepository {

    override fun getAllSetlists(): Flow<List<Setlist>> {
        return realm.query<SetlistDocument>().find()
            .asFlow()
            .map { resultsChange -> resultsChange.list.map { it.toGenericModel() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getSetlist(id: String): Setlist? {
        val objectId = ObjectId(id)
        val setlistDocument = realm.query<SetlistDocument>("_id == $0", objectId).first().find()
        return setlistDocument?.toGenericModel()
    }

    override suspend fun upsertSetlist(setlist: Setlist): Unit =
        realm.write {
            val setlistDocument = SetlistDocument(setlist)
            copyToRealm(setlistDocument, UpdatePolicy.ALL)
        }

    override suspend fun deleteSetlists(setlistIds: Collection<String>) =
        realm.write {
            setlistIds.map { ObjectId(it) }
                .mapNotNull { query<SetlistDocument>("_id == $0", it).first().find() }
                .forEach { delete(it) }
        }

}