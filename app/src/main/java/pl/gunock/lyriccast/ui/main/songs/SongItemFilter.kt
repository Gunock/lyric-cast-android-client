/*
 * Created by Tomasz Kiljanczyk on 26/01/2023, 21:30
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 26/01/2023, 21:30
 */

package pl.gunock.lyriccast.ui.main.songs

import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.misc.ItemFilter

class SongItemFilter : ItemFilter<SongItem, SongItemFilter.Values>() {
    class Values(
        val songTitle: MutableStateFlow<String> = MutableStateFlow(""),
        val categoryId: MutableStateFlow<String?> = MutableStateFlow(null)
    )

    override val values: Values = Values()

    private val categoryId get() = values.categoryId.value
    private val songTitle get() = values.songTitle.value

    override fun apply(collection: Collection<SongItem>): Collection<SongItem> {
        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (!categoryId.isNullOrBlank()) {
            predicates.add { songItem -> songItem.song.category?.id == categoryId }
        }

        if (songTitle.isNotBlank()) {
            val normalizedTitle = songTitle.trim().normalize()
            predicates.add { item ->
                item.normalizedTitle.contains(normalizedTitle, ignoreCase = true)
            }
        }

        return collection.filter { songItem ->
            predicates.all { predicate -> predicate(songItem) }
        }.toSortedSet()
    }
}