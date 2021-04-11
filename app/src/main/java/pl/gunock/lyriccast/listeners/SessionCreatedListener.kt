/*
 * Created by Tomasz Kiljanczyk on 4/11/21 2:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 1:00 PM
 */

package pl.gunock.lyriccast.listeners

import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener

class SessionCreatedListener(
    private val mListener: (session: Session) -> Unit
) : SessionManagerListener<Session> {

    override fun onSessionStarting(session: Session) {
    }

    override fun onSessionStarted(session: Session, sessionId: String) {
        mListener(session)
    }

    override fun onSessionStartFailed(session: Session, error: Int) {
    }

    override fun onSessionEnding(session: Session) {
    }

    override fun onSessionEnded(session: Session, error: Int) {
    }

    override fun onSessionResuming(session: Session, sessionId: String) {
    }

    override fun onSessionResumed(session: Session, wasSuspended: Boolean) {
    }

    override fun onSessionResumeFailed(session: Session, error: Int) {
    }

    override fun onSessionSuspended(session: Session, reason: Int) {
    }
}