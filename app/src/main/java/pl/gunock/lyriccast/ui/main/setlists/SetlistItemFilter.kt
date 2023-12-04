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