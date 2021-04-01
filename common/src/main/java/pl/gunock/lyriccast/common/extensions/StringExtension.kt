/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/30/21 9:05 PM
 */

package pl.gunock.lyriccast.common.extensions

import org.apache.commons.lang3.StringUtils

fun String.normalize(): String {
    return StringUtils.stripAccents(this)
}
