/*
 * Created by Tomasz Kiljanczyk on 26/01/2023, 23:20
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 26/01/2023, 23:17
 */

package pl.gunock.lyriccast.ui.shared.misc

abstract class ItemFilter<T, S> {
    abstract val values: S

    abstract fun apply(items: Collection<T>): Collection<T>
}