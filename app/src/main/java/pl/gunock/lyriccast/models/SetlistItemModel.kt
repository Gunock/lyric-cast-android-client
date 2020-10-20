/*
 * Created by Tomasz Kiljańczyk on 10/20/20 10:55 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/20/20 9:18 PM
 */

package pl.gunock.lyriccast.models

class SetlistItemModel(position: Int, setlist: SetlistModel) : SetlistModel(setlist) {
    var originalPosition: Int = position
}