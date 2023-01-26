/*
 * Created by Tomasz Kiljanczyk on 26/01/2023, 21:59
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 26/01/2023, 21:58
 */

package pl.gunock.lyriccast.ui.shared.misc

abstract class ItemFilter<T, S> {
    abstract val values: S

    abstract fun apply(collection: Collection<T>): Collection<T>
}