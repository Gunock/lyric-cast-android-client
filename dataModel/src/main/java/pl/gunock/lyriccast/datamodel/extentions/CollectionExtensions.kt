/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 14:29
 */

package pl.gunock.lyriccast.datamodel.extentions

import io.realm.RealmList

internal inline fun <reified T> Iterable<T>.toRealmList(): RealmList<T> {
    return when (this) {
        is Collection -> {
            RealmList(*this.toTypedArray())
        }
        else -> {
            RealmList(*this.toList().toTypedArray())
        }
    }
}

internal inline fun <T, reified R> Iterable<T>.mapRealmList(
    transform: (T) -> R
): RealmList<R> {
    return RealmList(*this.map(transform).toTypedArray())
}