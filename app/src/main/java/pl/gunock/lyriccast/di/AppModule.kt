/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 24/08/2021, 20:11
 */

package pl.gunock.lyriccast.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideDataTransferRepository(@ApplicationContext context: Context): DataTransferRepository {
        return RepositoryFactory.createDataTransferRepository(
            context.resources,
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