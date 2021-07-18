/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:16
 */

package pl.gunock.lyriccast.ui.shared.listeners

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