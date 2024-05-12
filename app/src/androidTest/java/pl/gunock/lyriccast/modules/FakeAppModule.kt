/*
 * Created by Tomasz Kiljanczyk on 10/01/2023, 21:34
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 10/01/2023, 21:31
 */

@file:Suppress("unused")

package pl.gunock.lyriccast.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.gunock.lyriccast.application.AppSettings
import pl.gunock.lyriccast.application.AppSettingsSerializer
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.di.AppModule
import pl.gunock.lyriccast.repositories.CategoriesRepositoryFakeImpl
import pl.gunock.lyriccast.repositories.DataTransferRepositoryFakeImpl
import pl.gunock.lyriccast.repositories.SetlistsRepositoryFakeImpl
import pl.gunock.lyriccast.repositories.SongsRepositoryFakeImpl
import java.io.File
import java.util.UUID
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class FakeAppModule {

    companion object {
        private const val TEST_DATASTORE_FILENAME = "settings"

        var dataStore: DataStore<AppSettings>? = null
        var dataStoreFile: File? = null

        fun initializeDataStore(appContext: Context) {
            if (dataStoreFile == null) {
                cleanupDataStore()
            }

            val newDataStoreFile =
                appContext.dataStoreFile("$TEST_DATASTORE_FILENAME-${UUID.randomUUID()}")
            dataStoreFile = newDataStoreFile

            dataStore = DataStoreFactory.create(
                produceFile = { newDataStoreFile },
                serializer = AppSettingsSerializer
            )
        }

        fun cleanupDataStore() {
            dataStoreFile?.delete()
            dataStoreFile = null
            dataStore = null
        }
    }


    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext appContext: Context): DataStore<AppSettings> {
        if (dataStore == null) {
            initializeDataStore(appContext)
        }

        return dataStore!!
    }

    @Provides
    @Singleton
    fun provideDataTransferRepository(): DataTransferRepository =
        DataTransferRepositoryFakeImpl(
            provideSongsRepository(),
            provideSetlistsRepository(),
            provideCategoriesRepository()
        )

    @Provides
    @Singleton
    fun provideSongsRepository(): SongsRepository = SongsRepositoryFakeImpl()

    @Provides
    @Singleton
    fun provideSetlistsRepository(): SetlistsRepository = SetlistsRepositoryFakeImpl()

    @Provides
    @Singleton
    fun provideCategoriesRepository(): CategoriesRepository = CategoriesRepositoryFakeImpl()
}