/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 21:06
 */

package pl.gunock.lyriccast.shared.extensions

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

fun Fragment.registerForActivityResult(
    callback: ActivityResultCallback<ActivityResult>
): ActivityResultLauncher<Intent> {
    return this.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        callback
    )
}