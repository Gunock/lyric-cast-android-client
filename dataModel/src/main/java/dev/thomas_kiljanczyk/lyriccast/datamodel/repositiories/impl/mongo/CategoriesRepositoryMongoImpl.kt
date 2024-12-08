/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.CategoryDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

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