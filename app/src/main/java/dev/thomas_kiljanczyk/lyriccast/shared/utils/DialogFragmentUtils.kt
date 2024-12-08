/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.shared.utils

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import dev.thomas_kiljanczyk.lyriccast.ui.shared.fragments.ProgressDialogFragment

object DialogFragmentUtils {

    fun createProgressDialogFragment(
        fragmentManager: FragmentManager,
        @StringRes messageResourceId: Int
    ): ProgressDialogFragment {
        val dialogFragment = ProgressDialogFragment(messageResourceId).apply {
            show(fragmentManager, ProgressDialogFragment.TAG)
        }

        return dialogFragment
    }

}