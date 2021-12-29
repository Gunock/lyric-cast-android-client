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
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository

internal class CategoriesRepositoryMongoImpl(
    private val dispatcher: CoroutineDispatcher
) : CategoriesRepository {

    private val realm: Realm = runBlocking(dispatcher) {
        Realm.getInstance(RealmConfiguration.Builder().build())
    }

    override fun getAllCategories(): Flowable<List<Category>> =
        runBlocking(dispatcher) {
            realm.where<CategoryDocument>().findAllAsync()
                .asFlowable()
                .map { categoryDocuments ->
                    categoryDocuments.map { it.toGenericModel() }
                }
        }

    override suspend fun upsertCategory(category: Category) =
        withContext(dispatcher) {
            val categoryDocument = CategoryDocument(category)
            realm.executeTransaction { it.insertOrUpdate(categoryDocument) }
        }

    override suspend fun deleteCategories(categoryIds: Collection<String>) =
        withContext(dispatcher) {
            realm.executeTransaction { transactionRealm ->
                for (id in categoryIds) {
                    transactionRealm.where<CategoryDocument>().findAll()
                        .where()
                        .equalTo("id", ObjectId(id))
                        .findFirst()
                        ?.deleteFromRealm()
                }
            }
        }

}