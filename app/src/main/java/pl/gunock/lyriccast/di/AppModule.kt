/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 22:33
 */

package pl.gunock.lyriccast.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.gunock.lyriccast.application.AppSettings
import pl.gunock.lyriccast.application.settingsDataStore
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext appContext: Context): DataStore<AppSettings> {
        return appContext.settingsDataStore
    }

    @Provides
    @Singleton
    fun provideDataTransferRepository(): DataTransferRepository {
        return RepositoryFactory.createDataTransferRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    @Singleton
    fun provideSongsRepository(): SongsRepository {
        return RepositoryFactory.createSongsRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    @Singleton
    fun provideSetlistsRepository(): SetlistsRepository {
        return RepositoryFactory.createSetlistsRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Provides
    @Singleton
    fun provideCategoriesRepository(): CategoriesRepository {
        return RepositoryFactory.createCategoriesRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

}