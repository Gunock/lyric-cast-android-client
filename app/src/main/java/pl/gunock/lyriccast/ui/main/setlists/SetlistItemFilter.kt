/*
 * Created by Tomasz Kiljanczyk on 26/01/2023, 21:30
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 26/01/2023, 21:30
 */

package pl.gunock.lyriccast.ui.main.setlists

import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.misc.ItemFilter

class SetlistItemFilter : ItemFilter<SetlistItem, SetlistItemFilter.Values>() {
    class Values(
        val setlistName: MutableStateFlow<String> = MutableStateFlow(""),
    )

    override val values: Values = Values()

    private val setlistName get() = values.setlistName.value

    override fun apply(items: Collection<SetlistItem>): Collection<SetlistItem> {
        if (setlistName.isBlank()) {
            return items
        }

        val normalizedTitle = setlistName.trim().normalize()
        val filteredItems = items.filter { setlistItem ->
            setlistItem.normalizedName.contains(normalizedTitle, ignoreCase = true)
        }

        return filteredItems
    }
}