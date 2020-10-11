/*
 * Created by Tomasz Kiljańczyk on 10/11/20 11:21 PM
 * Copyright (c) 2020 . All rights reserved.
 *  Last modified 10/11/20 11:06 PM
 */

package pl.gunock.lyriccast.utils

import android.content.Context

object ResourceHelper {

    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context
    }

    fun getString(id: Int): String {
        return context!!.getString(id)
    }

}