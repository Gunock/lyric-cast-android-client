/*
 * Created by Tomasz Kiljanczyk on 4/2/21 11:52 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/2/21 3:21 PM
 */

package pl.gunock.lyriccast.datamodel.extensions

internal fun Long?.toNonNullable(): Long {
    return this ?: Long.MIN_VALUE
}
