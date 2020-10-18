/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/16/20 9:45 PM
 */

package pl.gunock.lyriccast.utils

import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.utils.ResourceHelper.getString

object MessageHelper {

    private const val tag = "MessageHelper"

    private val CONTENT_NAMESPACE: String = getString(R.string.content_namespace)
    private val CONTROL_NAMESPACE: String = getString(R.string.control_namespace)
    private val CONTENT_MESSAGE_TEMPLATE: String = getString(R.string.content_message_template)
    private val CONTROL_MESSAGE_TEMPLATE: String = getString(R.string.control_message_template)

    fun sendContentMessage(context: CastContext, message: String) {
        val castSession = context.sessionManager.currentCastSession
        val messageContent = CONTENT_MESSAGE_TEMPLATE.format(message)
            .replace("\n", "<br>")
            .replace("\r", "")

        Log.d(tag, "Sending content message")
        Log.d(tag, "Namespace: $CONTENT_NAMESPACE")
        Log.d(tag, "Content: $messageContent")
        if (castSession == null) {
            Log.d(tag, "Message not sent (no session)")
            return
        }

        castSession.sendMessage(CONTENT_NAMESPACE, messageContent)
    }

    fun sendControlMessage(context: CastContext, action: ControlAction) {
        return sendControlMessage(context, action, null)
    }

    fun sendControlMessage(context: CastContext, action: ControlAction, value: String?) {
        val castSession = context.sessionManager.currentCastSession
        val messageContent = CONTROL_MESSAGE_TEMPLATE.format(action.toString(), value)

        Log.d(tag, "Sending control message")
        Log.d(tag, "Namespace: $CONTROL_NAMESPACE")
        Log.d(tag, "Content: $messageContent")
        if (castSession == null) {
            Log.d(tag, "Message not sent (no session)")
            return
        }

        castSession.sendMessage(CONTROL_NAMESPACE, messageContent)
    }
}