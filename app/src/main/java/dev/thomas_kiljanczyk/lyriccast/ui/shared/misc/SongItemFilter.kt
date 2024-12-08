/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.misc

import dev.thomas_kiljanczyk.lyriccast.common.extensions.normalize
import dev.thomas_kiljanczyk.lyriccast.domain.models.SongItem
import kotlinx.coroutines.flow.MutableStateFlow

class SongItemFilter : ItemFilter<SongItem, SongItemFilter.Values>() {
    override val values: Values = Values()

    override fun apply(items: Collection<SongItem>): Collection<SongItem> {
        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (values.isSelected != null) {
            predicates.add { songItem -> songItem.isSelected }
        }

        if (!values.categoryId.isNullOrBlank()) {
            predicates.add { songItem -> songItem.song.category?.id == values.categoryId }
        }

        if (values.songTitle.isNotBlank()) {
            val normalizedTitle = values.songTitle.trim().normalize()
            predicates.add { item ->
                item.normalizedTitle.contains(normalizedTitle, ignoreCase = true)
            }
        }

        if (predicates.isEmpty()) {
            return items
        }

        return items.filter { songItem ->
            predicates.all { predicate -> predicate(songItem) }
        }.toSortedSet()
    }


    class Values(
        val songTitleFlow: MutableStateFlow<String> = MutableStateFlow(""),
        val categoryIdFlow: MutableStateFlow<String?> = MutableStateFlow(null),
        val isSelectedFlow: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    ) {
        var songTitle
            get() = songTitleFlow.value
            set(value) {
                songTitleFlow.value = value
            }
        var categoryId
            get() = categoryIdFlow.value
            set(value) {
                categoryIdFlow.value = value
            }
        var isSelected
            get() = isSelectedFlow.value
            set(value) {
                isSelectedFlow.value = value
            }
    }
}