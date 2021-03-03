/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 10:59 PM
 */

package pl.gunock.lyriccast.listeners

import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener

class SessionCreatedListener(
    private val mListener: (session: Session) -> Unit
) : SessionManagerListener<Session> {

    override fun onSessionStarting(session: Session?) {
    }

    override fun onSessionStarted(session: Session?, p1: String?) {
        mListener(session!!)
    }

    override fun onSessionStartFailed(session: Session?, p1: Int) {
    }

    override fun onSessionEnding(session: Session?) {
    }

    override fun onSessionEnded(session: Session?, p1: Int) {
    }

    override fun onSessionResuming(session: Session?, p1: String?) {
    }

    override fun onSessionResumed(session: Session?, p1: Boolean) {
    }

    override fun onSessionResumeFailed(session: Session?, p1: Int) {
    }

    override fun onSessionSuspended(session: Session?, p1: Int) {
    }
}