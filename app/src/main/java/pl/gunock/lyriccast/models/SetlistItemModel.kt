/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 3:43 PM
 */

package pl.gunock.lyriccast.models

// TODO: Remove setlist item class
class SetlistItemModel(position: Int, setlist: SetlistModel) : SetlistModel(setlist) {
    var originalPosition: Int = position
}