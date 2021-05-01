/*
 * Created by Tomasz Kiljanczyk on 5/1/21 10:34 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 5/1/21 10:33 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import pl.gunock.lyriccast.R

class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setlist_editor)

        setSupportActionBar(findViewById(R.id.toolbar_setlist_editor))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val adView = findViewById<AdView>(R.id.adv_setlist_editor)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

}