/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 11/12/2021, 23:11
 */

package pl.gunock.lyriccast.shared.extensions

import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

fun AdView.loadAd() {
    // TODO: Add settings for ads
    val enableAds = context.getSettings().enableAds

    if (enableAds) {
        this.visibility = View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        this.loadAd(adRequest)
    } else {
        this.visibility = View.GONE
    }
}