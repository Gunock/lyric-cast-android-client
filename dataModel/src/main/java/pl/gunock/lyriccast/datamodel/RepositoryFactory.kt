/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 14:52
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/12/2021, 14:00
 */

package pl.gunock.lyriccast.datamodel

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import io.realm.Realm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.CategoriesRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.DataTransferRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SetlistsRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SongsRepositoryMongoImpl

object RepositoryFactory {

    fun initialize(context: Context, provider: RepositoryProvider) {
        return when (provider) {
            RepositoryProvider.MONGO -> Realm.init(context)
        }
    }

    fun createSongsRepository(provider: RepositoryProvider): SongsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val dispatcher = createDispatcher("SongsRepositoryMongoImpl")
                SongsRepositoryMongoImpl(dispatcher)
            }
        }
    }

    fun createSetlistsRepository(provider: RepositoryProvider): SetlistsRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val dispatcher = createDispatcher("SetlistsRepositoryMongoImpl")
                SetlistsRepositoryMongoImpl(dispatcher)
            }
        }
    }

    fun createCategoriesRepository(provider: RepositoryProvider): CategoriesRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val dispatcher = createDispatcher("CategoriesRepositoryMongoImpl")
                CategoriesRepositoryMongoImpl(dispatcher)
            }
        }
    }

    fun createDataTransferRepository(
        provider: RepositoryProvider
    ): DataTransferRepository {
        return when (provider) {
            RepositoryProvider.MONGO -> {
                val dispatcher = createDispatcher("DataTransferRepositoryMongoImpl")
                DataTransferRepositoryMongoImpl(dispatcher)
            }
        }
    }

    private fun createDispatcher(name: String): CoroutineDispatcher {
        val handlerThread = HandlerThread(name).also { it.start() }
        return Handler(handlerThread.looper).asCoroutineDispatcher()
    }

    enum class RepositoryProvider {
        MONGO
    }

}