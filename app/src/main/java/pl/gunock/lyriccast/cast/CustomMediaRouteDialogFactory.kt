/*
 * Created by Tomasz Kiljanczyk on 4/4/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 10:58 PM
 */

package pl.gunock.lyriccast.cast

import androidx.mediarouter.app.MediaRouteControllerDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory

class CustomMediaRouteDialogFactory : MediaRouteDialogFactory() {

    override fun onCreateControllerDialogFragment(): MediaRouteControllerDialogFragment {
        return CustomMediaRouteControllerDialogFragment()
    }

}