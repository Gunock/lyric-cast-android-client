/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.ui.shared.listeners

import com.google.android.material.tabs.TabLayout

class ItemSelectedTabListener(
    private val listener: (tab: TabLayout.Tab?) -> Unit
) : TabLayout.OnTabSelectedListener {

    override fun onTabSelected(tab: TabLayout.Tab?) {
        listener(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }
}