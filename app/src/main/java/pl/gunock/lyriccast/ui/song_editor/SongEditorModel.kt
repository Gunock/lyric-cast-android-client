/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 17:30
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 17:15
 */

package pl.gunock.lyriccast.ui.song_editor

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.shared.enums.NameValidationState
import javax.inject.Inject
import kotlin.collections.set

@HiltViewModel
class SongEditorModel @Inject constructor(
        @ApplicationContext context: Context,
        categoriesRepository: CategoriesRepository,
        private val songsRepository: SongsRepository
) : ViewModel() {

    private companion object {
        const val TAG = "SongEditorModel"
    }

    private var isEditingSong: Boolean = false

    private var songId: String? = null
    var songTitle: String = ""
    var category: Category? = null
    var presentation: List<String>? = null

    private var editedSong: Song? = null

    private val _categories: MutableSharedFlow<List<CategoryItem>> = MutableSharedFlow(replay = 1)
    val categories: Flow<List<CategoryItem>> get() = _categories

    private var songTitles: Set<String> = setOf()

    val categoryNone: CategoryItem = CategoryItem(
        Category(name = context.getString(R.string.category_none))
    )

    val newSectionName: String
        get() {
            val result = _newSectionName.format(newSectionCount)
            newSectionCount++
            return result
        }
    private val _newSectionName: String =
            context.getString(R.string.song_editor_input_new_section_template)

    private val sectionLyrics: MutableMap<String, String> = mutableMapOf()
    private val sectionCountMap: MutableMap<String, Int> = mutableMapOf()

    private var newSectionCount = 1

    init {
        songsRepository.getAllSongs()
                .onEach { songs -> songTitles = songs.map { it.title }.toSet() }
                .flowOn(Dispatchers.Default)
                .launchIn(viewModelScope)

        categoriesRepository.getAllCategories()
                .onEach {
                    val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                    _categories.emit(categoryItems)
                }.flowOn(Dispatchers.Default)
                .launchIn(viewModelScope)
    }

    fun validateSongTitle(songTitle: String): NameValidationState {
        if (songTitle.isBlank()) {
            return NameValidationState.EMPTY
        }

        val alreadyInUse = editedSong?.title != songTitle && songTitle in songTitles

        return if (alreadyInUse) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    fun loadSong(songId: String) {
        val song = songsRepository.getSong(songId)!!

        Log.v(TAG, "Received song : $song")

        editedSong = song
        isEditingSong = true
        this.songId = song.id
        songTitle = song.title
        category = song.category
        presentation = song.presentation

        for (sectionName in song.presentation) {
            setSectionText(sectionName, song.lyricsMap[sectionName]!!)
        }
    }

    suspend fun saveSong(presentation: List<String>) {
        val lyricsSections = sectionLyrics.map { Song.LyricsSection(it.key, it.value) }

        val song = Song(
                songId ?: "",
                songTitle,
                lyricsSections,
                presentation,
                category
        )

        songsRepository.upsertSong(song)
    }

    fun setupSection(sectionName: String) {
        sectionLyrics[sectionName] = ""
        sectionCountMap[sectionName] = 1
    }

    fun setSectionText(sectionName: String, sectionText: String) {
        sectionLyrics[sectionName] = sectionText
    }

    fun getSectionText(sectionName: String): String {
        return sectionLyrics[sectionName]!!
    }

    fun modifySectionCount(sectionName: String) {
        if (sectionCountMap.containsKey(sectionName)) {
            sectionCountMap[sectionName] = sectionCountMap[sectionName]!! + 1
        } else {
            sectionCountMap[sectionName] = 1
        }
    }

    fun removeSection(sectionName: String): Boolean {
        val tabCount = sectionCountMap[sectionName]!! - 1
        if (tabCount <= 0) {
            sectionCountMap.remove(sectionName)
            sectionLyrics.remove(sectionName)
        } else {
            sectionCountMap[sectionName] = tabCount
        }

        val totalSectionCount = sectionCountMap.values
                .toList()
                .sum()

        if (totalSectionCount <= 1) {
            newSectionCount = 1
        }

        return totalSectionCount > 0
    }

    fun calculateNewSectionCount(newSectionName: String) {
        while (
                sectionCountMap.keys.any { sectionName ->
                    sectionName.contains(newSectionName)
                            && sectionName.split(" ").last() == newSectionCount.toString()
                }
        ) {
            newSectionCount++
        }
    }

    fun decreaseSectionCount(sectionName: String) {
        val oldSectionCount = sectionCountMap[sectionName]!!
        if (oldSectionCount <= 1) {
            sectionLyrics.remove(sectionName)
            sectionCountMap.remove(sectionName)
        } else {
            sectionCountMap[sectionName] = oldSectionCount - 1
        }
    }

    fun increaseSectionCount(sectionName: String): Boolean {
        sectionCountMap[sectionName] =
                if (sectionCountMap.containsKey(sectionName)) {
                    sectionCountMap[sectionName]!! + 1
                } else {
                    1
                }

        return sectionLyrics.containsKey(sectionName)
    }

}