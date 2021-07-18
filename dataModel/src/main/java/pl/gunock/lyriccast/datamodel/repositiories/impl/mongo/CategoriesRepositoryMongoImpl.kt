/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 22:34
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.where
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository

internal class CategoriesRepositoryMongoImpl(
    private val mRealm: Realm = Realm.getInstance(
        RealmConfiguration.Builder()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
    )
) : CategoriesRepository {

    private val allCategories: RealmResults<CategoryDocument> =
        mRealm.where<CategoryDocument>().findAllAsync()

    override fun getAllCategories(): Flowable<List<Category>> {
        return allCategories.asFlowable()
            .map { songDocuments ->
                songDocuments.map { it.toGenericModel() }
            }
    }

    override fun upsertCategory(category: Category) {
        val categoryDocument = CategoryDocument(category)
        mRealm.executeTransaction { mRealm.insertOrUpdate(categoryDocument) }
    }

    override fun deleteCategories(categoryIds: Collection<String>) {
        mRealm.executeTransaction {
            for (id in categoryIds) {
                allCategories.where()
                    .equalTo("id", ObjectId(id))
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

    override fun close() {
        mRealm.close()
    }
}