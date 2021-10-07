/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 22:33
 */

package pl.gunock.lyriccast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideDataTransferRepository(): DataTransferRepository {
        return RepositoryFactory.createDataTransferRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    fun provideSongsRepository(): SongsRepository {
        return RepositoryFactory.createSongsRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    fun provideSetlistsRepository(): SetlistsRepository {
        return RepositoryFactory.createSetlistsRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    fun provideCategoriesRepository(): CategoriesRepository {
        return RepositoryFactory.createCategoriesRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

}