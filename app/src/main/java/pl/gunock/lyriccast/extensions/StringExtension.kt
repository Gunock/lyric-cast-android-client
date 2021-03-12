/*
 * Created by Tomasz Kiljańczyk on 3/12/21 4:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 3:07 PM
 */

package pl.gunock.lyriccast.extensions

import org.apache.commons.lang3.StringUtils

fun String.normalize(): String {
    return StringUtils.stripAccents(this)
}
