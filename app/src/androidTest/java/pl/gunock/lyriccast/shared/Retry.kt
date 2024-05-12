/*
 * Created by Tomasz Kiljanczyk on 5/12/24, 10:24 PM
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 5/12/24, 10:24 PM
 */

package pl.gunock.lyriccast.shared

import java.lang.Thread.sleep


fun <T> retryWithTimeout(
    retries: Int = 10,
    retryDelayMs: Long = 100,
    action: () -> T
): T {
    repeat(retries) { i ->
        try {
            return action()
        } catch (e: Throwable) {
            if (i >= retries) {
                throw e
            } else {
                sleep(retryDelayMs)
            }
        }
    }

    return action()
}