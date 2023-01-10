/*
 * Created by Tomasz Kiljanczyk on 10/01/2023, 21:34
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 10/01/2023, 21:31
 */

@file:Suppress("unused")

package pl.gunock.lyriccast.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.di.AppModule
import pl.gunock.lyriccast.repositories.CategoriesRepositoryFakeImpl
import pl.gunock.lyriccast.repositories.DataTransferRepositoryFakeImpl
import pl.gunock.lyriccast.repositories.SetlistsRepositoryFakeImpl
import pl.gunock.lyriccast.repositories.SongsRepositoryFakeImpl
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class FakeAppModule {


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