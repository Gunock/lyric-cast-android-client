/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 4:51 PM
 */

package pl.gunock.lyriccast.datamodel

import android.content.res.Resources
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SongDto


class DatabaseViewModel(
    resources: Resources,
    private val mRepository: LyricCastRepository
) : ViewModel() {
    val allSetlists: LiveData<List<Setlist>> = mRepository.allSetlists.asLiveData()

    private val mDataTransferProcessor = DataTransferProcessor(resources, mRepository)

    fun removeObservers(lifecycleOwner: LifecycleOwner) {
        allSetlists.removeObservers(lifecycleOwner)
    }

    fun upsertSetlist(setlist: SetlistWithSongs) =
        viewModelScope.launch { mRepository.upsertSetlist(setlist) }

    fun deleteSetlists(setlistIds: List<Long>) =
        viewModelScope.launch { mRepository.deleteSetlists(setlistIds) }

    suspend fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        mDataTransferProcessor.importSongs(data, message, options)
    }

    suspend fun importSongs(
        songDtoSet: Set<SongDto>,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        val categoryDtos: List<CategoryDto> = songDtoSet.map { songDto -> songDto.category }
            .distinct()
            .mapIndexedNotNull { index, categoryName ->
                CategoryDto(
                    name = categoryName?.take(30) ?: return@mapIndexedNotNull null,
                    color = options.colors[index % options.colors.size]
                )
            }

        val data = DatabaseTransferData(songDtoSet.toList(), categoryDtos, null)
        mDataTransferProcessor.importSongs(data, message, options)
    }

    suspend fun getDatabaseTransferData(): DatabaseTransferData {
        return mDataTransferProcessor.getDatabaseTransferData()
    }

    class Factory(
        private val mResources: Resources,
        private val mRepository: LyricCastRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DatabaseViewModel(mResources, mRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

