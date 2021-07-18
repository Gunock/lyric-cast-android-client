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
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository

internal class SetlistsRepositoryMongoImpl(
    private val mRealm: Realm = Realm.getInstance(
        RealmConfiguration.Builder()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
    )
) : SetlistsRepository {

    private val mAllSetlists: RealmResults<SetlistDocument> =
        mRealm.where<SetlistDocument>().findAllAsync()

    override fun getAllSetlists(): Flowable<List<Setlist>> {
        return mAllSetlists.asFlowable()
            .map { setlistDocuments ->
                setlistDocuments.map { it.toGenericModel() }
            }
    }

    override fun getSetlist(id: String): Setlist? {
        val setlistDocument = mRealm.where<SetlistDocument>()
            .equalTo("id", ObjectId(id))
            .findFirst()

        return setlistDocument?.toGenericModel()
    }

    override fun upsertSetlist(setlist: Setlist) {
        val setlistDocument = SetlistDocument(setlist)

        mRealm.executeTransaction { mRealm.insertOrUpdate(setlistDocument) }
    }

    override fun deleteSetlists(setlistIds: Collection<String>) {
        for (id in setlistIds) {
            mAllSetlists.where()
                .equalTo("id", id)
                .findFirst()
                ?.deleteFromRealm()
        }
    }

}