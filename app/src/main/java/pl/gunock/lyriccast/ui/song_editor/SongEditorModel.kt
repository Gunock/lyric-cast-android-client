/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 14:52
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 29/12/2021, 14:52
 */

package pl.gunock.lyriccast.ui.song_editor

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.disposables.Disposable
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

    val categories: LiveData<List<CategoryItem>> get() = _categories
    private val _categories: MutableLiveData<List<CategoryItem>> = MutableLiveData(listOf())

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

    private var songsSubscription: Disposable? = null
    private var categoriesSubscription: Disposable? = null

    init {
        songsSubscription = songsRepository.getAllSongs()
            .subscribe { songs ->
                songTitles = songs.map { it.title }.toSet()
            }

        categoriesSubscription = categoriesRepository.getAllCategories().subscribe { categories1 ->
            _categories.postValue(categories1.map { CategoryItem(it) })
        }
    }

    override fun onCleared() {
        songsSubscription?.dispose()
        categoriesSubscription?.dispose()
        super.onCleared()
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

    fun setUpSection(sectionName: String) {
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

        return if (totalSectionCount > 2) {
            true
        } else {
            newSectionCount = 1
            false
        }
    }

    fun calculateNewSectionCount(newSectionName: String) {
        while (
            sectionCountMap.keys.any { sectionName ->
                sectionName.contains(newSectionName)
                        && sectionName.split(" ")
                    .last() == newSectionCount.toString()
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