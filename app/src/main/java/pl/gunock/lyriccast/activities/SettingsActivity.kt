/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/17/20 12:30 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.utils.ResourceHelper

class SettingsActivity : AppCompatActivity() {
    private val tag = "SettingsActivity"

    private var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ResourceHelper.initialize(applicationContext)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar_settings))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        castContext = CastContext.getSharedInstance(this)
    }

}