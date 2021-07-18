/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 12:26
 */

package pl.gunock.lyriccast.shared.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import pl.gunock.lyriccast.R

@Suppress("unused")
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val mediaOptions = CastMediaOptions.Builder()
            .setMediaSessionEnabled(false)
            .build()

        return CastOptions.Builder()
            .setCastMediaOptions(mediaOptions)
            .setReceiverApplicationId(context.getString(R.string.chromecast_app_id))
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}