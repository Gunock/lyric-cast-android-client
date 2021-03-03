/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:00 PM
 */

package pl.gunock.lyriccast.listeners

import com.google.android.material.tabs.TabLayout

class ItemSelectedTabListener(
    private val mListener: (tab: TabLayout.Tab?) -> Unit
) : TabLayout.OnTabSelectedListener {

    override fun onTabSelected(tab: TabLayout.Tab?) {
        mListener(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }
}