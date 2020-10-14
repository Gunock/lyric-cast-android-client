/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 11:44 PM
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