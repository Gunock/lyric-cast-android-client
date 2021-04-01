/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 2:24 AM
 */

package pl.gunock.lyriccast.datamodel.extensions

fun Long?.toNonNullable(): Long {
    return this ?: Long.MIN_VALUE
}
