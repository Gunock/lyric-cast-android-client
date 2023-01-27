/*
 * Created by Tomasz Kiljanczyk on 26/01/2023, 23:41
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 26/01/2023, 23:32
 */

package pl.gunock.lyriccast.ui.shared.misc

import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.domain.models.SongItem

class SongItemFilter : ItemFilter<SongItem, SongItemFilter.Values>() {
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
}