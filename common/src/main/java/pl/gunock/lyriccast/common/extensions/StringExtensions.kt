/*
 * Created by Tomasz Kiljanczyk on 06/05/2021, 13:42
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/04/2021, 17:41
 */

package pl.gunock.lyriccast.common.extensions

import org.apache.commons.lang3.StringUtils

fun String.normalize(): String {
    return StringUtils.stripAccents(this)
}
