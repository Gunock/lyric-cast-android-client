/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/12/2021, 00:03
 */

package pl.gunock.lyriccast.shared.utils

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import pl.gunock.lyriccast.ui.shared.fragments.ProgressDialogFragment

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