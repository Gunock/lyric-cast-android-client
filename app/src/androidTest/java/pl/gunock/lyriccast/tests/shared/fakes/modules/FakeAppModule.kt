/*
 * Created by Tomasz Kiljanczyk on 09/01/2023, 01:38
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 09/01/2023, 01:38
 */

@file:Suppress("unused")

package pl.gunock.lyriccast.tests.shared.fakes.modules

import android.graphics.Color
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.di.AppModule
import pl.gunock.lyriccast.tests.shared.fakes.repositories.CategoriesRepositoryFakeImpl
import pl.gunock.lyriccast.tests.shared.fakes.repositories.SetlistsRepositoryFakeImpl
import pl.gunock.lyriccast.tests.shared.fakes.repositories.SongsRepositoryFakeImpl
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class FakeAppModule {

    companion object {
        val category1 = Category("ADD_CATEGORY_TEST 1", Color.RED)
    }

    // TODO: Implement fake
    @Singleton
    @Provides
    fun provideDataTransferRepository(): DataTransferRepository {
        return RepositoryFactory.createDataTransferRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )
    }

    @Singleton
    @Provides
    fun provideSongsRepository(): SongsRepository {
        return SongsRepositoryFakeImpl()
    }

    @Singleton
    @Provides
    fun provideSetlistsRepository(): SetlistsRepository {
        return SetlistsRepositoryFakeImpl()
    }

    @Singleton
    @Provides
    fun provideCategoriesRepository(): CategoriesRepository {
        val repository = CategoriesRepositoryFakeImpl()

        runBlocking { repository.upsertCategory(category1) }

        return repository
    }
}