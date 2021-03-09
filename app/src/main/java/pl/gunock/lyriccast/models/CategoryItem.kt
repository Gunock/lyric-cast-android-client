/*
 * Created by Tomasz Kiljańczyk on 3/8/21 11:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 10:47 PM
 */

package pl.gunock.lyriccast.models

class CategoryItem(
    category: Category,
    var isSelected: Boolean = false
) : Category(category) {

    constructor(name: String, color: Int) : this(Category(Long.MIN_VALUE, name, color))

}
