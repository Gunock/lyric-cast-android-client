/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.setlists

import dev.thomas_kiljanczyk.lyriccast.common.extensions.normalize
import dev.thomas_kiljanczyk.lyriccast.domain.models.SetlistItem
import dev.thomas_kiljanczyk.lyriccast.ui.shared.misc.ItemFilter
import kotlinx.coroutines.flow.MutableStateFlow

class SetlistItemFilter : ItemFilter<SetlistItem, SetlistItemFilter.Values>() {
    override val values: Values = Values()

    override fun apply(items: Collection<SetlistItem>): Collection<SetlistItem> {
        if (values.setlistName.isBlank()) {
            return items
        }

        val normalizedTitle = values.setlistName.trim().normalize()
        val filteredItems = items.filter { setlistItem ->
            setlistItem.normalizedName.contains(normalizedTitle, ignoreCase = true)
        }

        return filteredItems
    }


    class Values(
        val setlistNameFlow: MutableStateFlow<String> = MutableStateFlow("")
    ) {
        var setlistName
            get() = setlistNameFlow.value
            set(value) {
                setlistNameFlow.value = value
            }
    }
}