/*
 * Created by Tomasz Kiljańczyk on 3/8/21 10:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 9:41 PM
 */

package pl.gunock.lyriccast.models

class CategoryItem(
    category: Category,
    var isSelected: Boolean = false
) : Category(category.name, category.color) {

    constructor(name: String, color: Int) : this(Category(name, color))

}
