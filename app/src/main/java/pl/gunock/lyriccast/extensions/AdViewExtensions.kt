/*
 * Created by Tomasz Kiljanczyk on 17/07/2021, 11:19
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:05
 */

package pl.gunock.lyriccast.extensions

import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import pl.gunock.lyriccast.models.LyricCastSettings

fun AdView.loadAd() {
    if (LyricCastSettings.enableAds) {
        this.visibility = View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        this.loadAd(adRequest)
    } else {
        this.visibility = View.GONE
    }
}