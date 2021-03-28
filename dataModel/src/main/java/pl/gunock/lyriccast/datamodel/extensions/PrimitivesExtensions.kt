/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/27/21 9:57 PM
 */

package pl.gunock.lyriccast.datamodel.extensions

fun Long.toNullable(): Long? {
    return if (this != Long.MIN_VALUE) this else null
}

fun Long?.toNonNullable(): Long {
    return this ?: Long.MIN_VALUE
}
