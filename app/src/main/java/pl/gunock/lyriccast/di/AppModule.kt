/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 22:51
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
            RepositoryFactory.Provider.MONGO
        )
    }

    @Provides
    fun provideSongsRepository(): SongsRepository {
        return RepositoryFactory.createSongsRepository(
            RepositoryFactory.Provider.MONGO
        )
    }

    @Provides
    fun provideSetlistsRepository(): SetlistsRepository {
        return RepositoryFactory.createSetlistsRepository(
            RepositoryFactory.Provider.MONGO
        )
    }

    @Provides
    fun provideCategoriesRepository(): CategoriesRepository {
        return RepositoryFactory.createCategoriesRepository(
            RepositoryFactory.Provider.MONGO
        )
    }

}