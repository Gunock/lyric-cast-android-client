/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:16 PM
 */

package pl.gunock.lyriccast.listeners

import com.google.android.material.tabs.TabLayout

class TabItemSelectedListener(
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