/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.CategoryDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.SetlistDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.SongDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.CategoriesRepositoryMongoImpl
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.DataTransferRepositoryMongoImpl
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.SetlistsRepositoryMongoImpl
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.mongo.SongsRepositoryMongoImpl
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object RepositoryFactory {
    private val schema = setOf(
        LyricsSectionDocument::class,
        CategoryDocument::class,
        SongDocument::class,
        SetlistDocument::class,
    )

    fun createSongsRepository(provider: RepositoryProvider): SongsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val realm = openRealm()
                SongsRepositoryMongoImpl(realm)
            }
        }
    }

    fun createSetlistsRepository(provider: RepositoryProvider): SetlistsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val realm = openRealm()
                SetlistsRepositoryMongoImpl(realm)
            }
        }
    }

    fun createCategoriesRepository(provider: RepositoryProvider): CategoriesRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val realm = openRealm()
                CategoriesRepositoryMongoImpl(realm)
            }
        }
    }

    fun createDataTransferRepository(
        provider: RepositoryProvider
    ): DataTransferRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val realm = openRealm()
                DataTransferRepositoryMongoImpl(realm)
            }
        }
    }

    private fun openRealm(): Realm {
        return runBlocking(Dispatchers.IO) {
            val realmConfiguration = RealmConfiguration.Builder(schema).build()
            Realm.open(realmConfiguration)
        }
    }

    enum class RepositoryProvider {
        MONGO
    }

}