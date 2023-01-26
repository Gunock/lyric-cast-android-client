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
        val songTitle: MutableStateFlow<String> = MutableStateFlow(""),
        val categoryId: MutableStateFlow<String?> = MutableStateFlow(null),
        val isSelected: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    )

    override val values: Values = Values()

    private val categoryId get() = values.categoryId.value
    private val songTitle get() = values.songTitle.value
    private val isSelected get() = values.isSelected.value

    override fun apply(items: Collection<SongItem>): Collection<SongItem> {
        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (isSelected != null) {
            predicates.add { songItem -> songItem.isSelected }
        }

        if (!categoryId.isNullOrBlank()) {
            predicates.add { songItem -> songItem.song.category?.id == categoryId }
        }

        if (songTitle.isNotBlank()) {
            val normalizedTitle = songTitle.trim().normalize()
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