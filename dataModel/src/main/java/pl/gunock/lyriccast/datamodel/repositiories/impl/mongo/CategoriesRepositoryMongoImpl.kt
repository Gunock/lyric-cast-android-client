/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:06
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl.mongo

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    override fun getAllCategories(): Flow<List<Category>> =
        runBlocking(dispatcher) {
            realm.where<CategoryDocument>().findAllAsync()
                .toFlow()
                .map { categories -> categories.map { it.toGenericModel() } }
                .flowOn(dispatcher)
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