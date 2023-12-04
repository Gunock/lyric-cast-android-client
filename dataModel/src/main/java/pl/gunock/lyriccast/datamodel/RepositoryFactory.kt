/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 14:52
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/12/2021, 14:00
 */

package pl.gunock.lyriccast.datamodel

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SetlistDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.CategoriesRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.DataTransferRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SetlistsRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SongsRepositoryMongoImpl

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