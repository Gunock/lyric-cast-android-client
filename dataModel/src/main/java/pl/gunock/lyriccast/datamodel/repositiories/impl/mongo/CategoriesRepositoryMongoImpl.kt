/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:29
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository

internal class CategoriesRepositoryMongoImpl : CategoriesRepository {

    private val realm: Realm =
        Realm.getInstance(RealmConfiguration.Builder().build())

    override fun getAllCategories(): Flowable<List<Category>> {
        return realm.where<CategoryDocument>().findAllAsync()
            .asFlowable()
            .map { songDocuments ->
                songDocuments.freeze().map { it.toGenericModel() }
            }
    }

    override suspend fun upsertCategory(category: Category) {
        val categoryDocument = CategoryDocument(category)
        realm.executeTransactionAwait(Dispatchers.IO) { realm.insertOrUpdate(categoryDocument) }
    }

    override suspend fun deleteCategories(categoryIds: Collection<String>) {
        realm.executeTransactionAwait(Dispatchers.IO) { transactionRealm ->
            for (id in categoryIds) {
                transactionRealm.where<CategoryDocument>().findAllAsync()
                    .where()
                    .equalTo("id", ObjectId(id))
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

}