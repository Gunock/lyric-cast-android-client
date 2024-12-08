/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.SetlistDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

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