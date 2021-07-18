/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 23:28
 */

package pl.gunock.lyriccast.datamodel

import android.content.Context
import android.content.res.Resources
import io.realm.Realm
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.CategoriesRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.DataTransferRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SetlistsRepositoryMongoImpl
import pl.gunock.lyriccast.datamodel.repositiories.impl.mongo.SongsRepositoryMongoImpl

object RepositoryFactory {

    fun initialize(context: Context, provider: Provider) {
        return when (provider) {
            Provider.MONGO -> Realm.init(context)
        }
    }

    fun createSongsRepository(provider: Provider): SongsRepository {
        return when (provider) {
            Provider.MONGO -> SongsRepositoryMongoImpl()
        }
    }

    fun createSetlistsRepository(provider: Provider): SetlistsRepository {
        return when (provider) {
            Provider.MONGO -> SetlistsRepositoryMongoImpl()
        }
    }

    fun createCategoriesRepository(provider: Provider): CategoriesRepository {
        return when (provider) {
            Provider.MONGO -> CategoriesRepositoryMongoImpl()
        }
    }

    fun createDataTransferRepository(
        resources: Resources,
        provider: Provider
    ): DataTransferRepository {
        return when (provider) {
            Provider.MONGO -> DataTransferRepositoryMongoImpl(resources)
        }
    }


    enum class Provider {
        MONGO
    }

}