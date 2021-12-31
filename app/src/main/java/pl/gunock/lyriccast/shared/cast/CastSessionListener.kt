/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 18:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 18:15
 */

package pl.gunock.lyriccast.shared.cast

import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener

class CastSessionListener(
    private val onStarted: (session: Session) -> Unit,
    private val onEnded: ((session: Session) -> Unit)? = null
) : SessionManagerListener<Session> {

    override fun onSessionStarting(session: Session) {
    }

    override fun onSessionStarted(session: Session, sessionId: String) {
        onStarted(session)
    }

    override fun onSessionStartFailed(session: Session, error: Int) {
    }

    override fun onSessionEnding(session: Session) {
    }

    override fun onSessionEnded(session: Session, error: Int) {
        onEnded?.invoke(session)
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