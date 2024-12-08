/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.misc

abstract class ItemFilter<T, S> {
    abstract val values: S

    abstract fun apply(items: Collection<T>): Collection<T>
}