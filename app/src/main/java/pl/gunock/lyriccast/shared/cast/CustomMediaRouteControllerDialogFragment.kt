/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 12:26
 */

package pl.gunock.lyriccast.shared.cast

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