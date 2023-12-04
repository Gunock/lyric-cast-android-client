/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:06
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
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository

internal class CategoriesRepositoryMongoImpl(private val realm: Realm) : CategoriesRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return realm.query<CategoryDocument>().find()
            .asFlow()
            .map { resultsChange -> resultsChange.list.map { it.toGenericModel() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun upsertCategory(category: Category): Unit =
        realm.write {
            val categoryDocument = CategoryDocument(category)
            copyToRealm(categoryDocument, UpdatePolicy.ALL)
        }

    override suspend fun deleteCategories(categoryIds: Collection<String>) =
        realm.write {
            categoryIds.map { ObjectId(it) }
                .mapNotNull { query<CategoryDocument>("_id == $0", it).first().find() }
                .forEach { delete(it) }
        }

}