/*
 * Created by Tomasz Kiljanczyk on 4/4/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 11:43 PM
 */

package pl.gunock.lyriccast.cast

import android.content.Context
import android.os.Bundle
import androidx.mediarouter.app.MediaRouteControllerDialog
import androidx.mediarouter.app.MediaRouteControllerDialogFragment

class CustomMediaRouteControllerDialogFragment : MediaRouteControllerDialogFragment() {
    override fun onCreateControllerDialog(
        context: Context?,
        savedInstanceState: Bundle?
    ): MediaRouteControllerDialog {
        val dialog = super.onCreateControllerDialog(context, savedInstanceState)
        dialog.isVolumeControlEnabled = false
        dialog.setTitle("")
        return dialog
    }
}