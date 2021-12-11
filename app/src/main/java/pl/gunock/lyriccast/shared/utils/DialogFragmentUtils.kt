/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/12/2021, 00:03
 */

package pl.gunock.lyriccast.shared.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.delay
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.ui.shared.fragments.ProgressDialogFragment

object DialogFragmentUtils {

    suspend fun createProgressDialogFragment(
        fragmentManager: FragmentManager,
        messageResourceId: Int
    ): ProgressDialogFragment {
        val dialogFragment = ProgressDialogFragment().apply {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_LyricCast_Dialog)
            show(fragmentManager, ProgressDialogFragment.TAG)
        }

        while (!dialogFragment.hasBinding()) {
            delay(10)
        }
        dialogFragment.setMessage(messageResourceId)

        return dialogFragment
    }

}