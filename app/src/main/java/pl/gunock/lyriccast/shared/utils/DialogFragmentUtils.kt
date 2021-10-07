/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 15:19
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

        while (!dialogFragment.isAdded) {
            delay(10)
        }
        dialogFragment.setMessage(messageResourceId)

        return dialogFragment
    }

}