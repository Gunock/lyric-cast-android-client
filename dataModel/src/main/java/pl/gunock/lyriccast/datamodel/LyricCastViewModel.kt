/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 1:10 AM
 */

package pl.gunock.lyriccast.datamodel

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SongDto


class LyricCastViewModel(
    resources: Resources,
    private val repository: LyricCastRepository
) : ViewModel() {
    val allSongs: LiveData<List<SongAndCategory>> = repository.allSongs.asLiveData()
    val allSetlists: LiveData<List<Setlist>> = repository.allSetlists.asLiveData()
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    private val dataTransferProcessor = DataTransferProcessor(resources, repository)

    suspend fun upsertSong(song: SongWithLyricsSections, order: List<Pair<String, Int>>) =
        repository.upsertSong(song, order)

    fun deleteSongs(songIds: List<Long>) =
        viewModelScope.launch { repository.deleteSongs(songIds) }

    fun upsertSetlist(setlist: SetlistWithSongs) =
        viewModelScope.launch { repository.upsertSetlist(setlist) }

    fun deleteSetlists(setlistIds: List<Long>) =
        viewModelScope.launch { repository.deleteSetlists(setlistIds) }

    fun upsertCategory(category: Category) =
        runBlocking { repository.upsertCategory(category) }

    fun deleteCategories(categoryIds: Collection<Long>) =
        viewModelScope.launch { repository.deleteCategories(categoryIds) }


    suspend fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        dataTransferProcessor.importSongs(data, message, options)
    }

    suspend fun importSongs(
        songDtoSet: Set<SongDto>,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        val categoryDtos: List<CategoryDto> = songDtoSet.map { songDto -> songDto.category }
            .distinct()
            .mapIndexed { index, categoryName ->
                CategoryDto(
                    name = categoryName.take(30),
                    color = options.colors[index % options.colors.size]
                )
            }

        val data = DatabaseTransferData(songDtoSet.toList(), categoryDtos, null)
        dataTransferProcessor.importSongs(data, message, options)
    }

    suspend fun getDatabaseTransferData(): DatabaseTransferData {
        return dataTransferProcessor.getDatabaseTransferData()
    }

}

class LyricCastViewModelFactory(
    private val context: Context,
    private val repository: LyricCastRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LyricCastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LyricCastViewModel(context.resources, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}