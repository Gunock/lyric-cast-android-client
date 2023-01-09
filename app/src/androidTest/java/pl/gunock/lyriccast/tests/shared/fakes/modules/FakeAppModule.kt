/*
 * Created by Tomasz Kiljanczyk on 09/01/2023, 01:38
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 09/01/2023, 01:38
 */

@file:Suppress("unused")

package pl.gunock.lyriccast.tests.shared.fakes.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.di.AppModule
import pl.gunock.lyriccast.tests.shared.fakes.repositories.CategoriesRepositoryFakeImpl
import pl.gunock.lyriccast.tests.shared.fakes.repositories.DataTransferRepositoryFakeImpl
import pl.gunock.lyriccast.tests.shared.fakes.repositories.SetlistsRepositoryFakeImpl
import pl.gunock.lyriccast.tests.shared.fakes.repositories.SongsRepositoryFakeImpl
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