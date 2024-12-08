/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:06
 */

package dev.thomas_kiljanczyk.lyriccast.shared

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