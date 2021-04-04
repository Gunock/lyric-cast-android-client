/*
 * Created by Tomasz Kiljanczyk on 4/4/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 11:30 PM
 */

package pl.gunock.lyriccast.cast

import android.content.Context
import android.view.View
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.app.MediaRouteDialogFactory

class CustomMediaRouteActionProvider(context: Context) : MediaRouteActionProvider(context) {

    private var mFactory: MediaRouteDialogFactory = CustomMediaRouteDialogFactory()

    init {
        setAlwaysVisible(true)
    }

    override fun onCreateActionView(): View {
        val castButton = super.onCreateActionView() as MediaRouteButton
        castButton.dialogFactory = dialogFactory
        return castButton
    }

    override fun getDialogFactory(): MediaRouteDialogFactory {
        return mFactory
    }

    override fun setDialogFactory(factory: MediaRouteDialogFactory) {
        mFactory = factory
    }

}