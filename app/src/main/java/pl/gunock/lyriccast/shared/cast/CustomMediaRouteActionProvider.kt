/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.shared.cast

import android.content.Context
import android.view.View
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.app.MediaRouteDialogFactory

class CustomMediaRouteActionProvider(context: Context) : MediaRouteActionProvider(context) {

    private var factory: MediaRouteDialogFactory = CustomMediaRouteDialogFactory()

    override fun onCreateActionView(): View {
        val castButton = super.onCreateActionView() as MediaRouteButton
        castButton.dialogFactory = dialogFactory
        return castButton
    }

    override fun getDialogFactory(): MediaRouteDialogFactory {
        return factory
    }

    override fun setDialogFactory(factory: MediaRouteDialogFactory) {
        this.factory = factory
    }

}