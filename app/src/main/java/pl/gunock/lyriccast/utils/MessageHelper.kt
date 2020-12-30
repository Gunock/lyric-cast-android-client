/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/25/20 9:51 PM
 */

package pl.gunock.lyriccast.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R

object MessageHelper {

    private const val tag = "MessageHelper"

    private var CONTENT_NAMESPACE: String = ""
    private var CONTROL_NAMESPACE: String = ""
    private var CONTENT_MESSAGE_TEMPLATE: String = ""
    private var CONTROL_MESSAGE_TEMPLATE: String = ""

    fun initialize(context: Context) {
        CONTENT_NAMESPACE = context.getString(R.string.content_namespace)
        CONTROL_NAMESPACE = context.getString(R.string.control_namespace)
        CONTENT_MESSAGE_TEMPLATE = context.getString(R.string.content_message_template)
        CONTROL_MESSAGE_TEMPLATE = context.getString(R.string.control_message_template)
    }

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

    fun sendControlMessage(context: CastContext, action: ControlAction, json: JSONObject) {
        val castSession = context.sessionManager.currentCastSession
        val messageContent = CONTROL_MESSAGE_TEMPLATE.format(action.toString(), null)
        val messageJson = JSONObject(messageContent)
        messageJson.put("value", json)

        Log.d(tag, "Sending control message")
        Log.d(tag, "Namespace: $CONTROL_NAMESPACE")
        Log.d(tag, "Content: $messageJson")
        if (castSession == null) {
            Log.d(tag, "Message not sent (no session)")
            return
        }

        castSession.sendMessage(CONTROL_NAMESPACE, messageJson.toString())
    }
}