/*
 * Created by Tomasz Kiljańczyk on 10/12/20 10:37 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/12/20 10:37 PM
 */

package pl.gunock.lyriccast.listeners

import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener

class SessionCreatedListener(private val mListener: (session: Session) -> Unit) :
    SessionManagerListener<Session> {
    override fun onSessionStarting(p0: Session?) {
    }

    override fun onSessionStarted(p0: Session?, p1: String?) {
        mListener(p0!!)
    }

    override fun onSessionStartFailed(p0: Session?, p1: Int) {
    }

    override fun onSessionEnding(p0: Session?) {
    }

    override fun onSessionEnded(p0: Session?, p1: Int) {
    }

    override fun onSessionResuming(p0: Session?, p1: String?) {
    }

    override fun onSessionResumed(p0: Session?, p1: Boolean) {
    }

    override fun onSessionResumeFailed(p0: Session?, p1: Int) {
    }

    override fun onSessionSuspended(p0: Session?, p1: Int) {
    }
}