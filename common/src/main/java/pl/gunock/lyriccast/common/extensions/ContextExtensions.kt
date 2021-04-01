/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/30/21 9:05 PM
 */

package pl.gunock.lyriccast.common.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner

/**
 * Source: https://stackoverflow.com/questions/49075413/easy-way-to-get-current-activity-fragment-lifecycleowner-from-within-view
 */
fun Context.getLifecycleOwner(): LifecycleOwner? {
    var currentContext = this
    var maxDepth = 20
    while (maxDepth-- > 0 && currentContext !is LifecycleOwner) {
        currentContext = (currentContext as ContextWrapper).baseContext
    }
    return if (currentContext is LifecycleOwner) {
        currentContext
    } else {
        null
    }
}