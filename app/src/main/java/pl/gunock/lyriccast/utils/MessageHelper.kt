/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:51 PM
 */

package pl.gunock.lyriccast.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R

object MessageHelper {
    private const val TAG = "MessageHelper"

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

        Log.d(TAG, "Sending content message")
        Log.d(TAG, "Namespace: $CONTENT_NAMESPACE")
        Log.d(TAG, "Content: $messageContent")
        if (castSession == null) {
            Log.d(TAG, "Message not sent (no session)")
            return
        }

        castSession.sendMessage(CONTENT_NAMESPACE, messageContent)
    }

    fun sendControlMessage(context: CastContext, action: ControlAction) {
        sendControlMessage(context, action, null)
    }


    fun sendControlMessage(context: CastContext, action: ControlAction, json: JSONObject) {
        val castSession = context.sessionManager.currentCastSession
        val messageContent = CONTROL_MESSAGE_TEMPLATE.format(action.toString(), null)
        val messageJson = JSONObject(messageContent).apply {
            put("value", json)
        }

        Log.d(TAG, "Sending control message")
        Log.d(TAG, "Namespace: $CONTROL_NAMESPACE")
        Log.d(TAG, "Content: $messageJson")
        if (castSession == null) {
            Log.d(TAG, "Message not sent (no session)")
            return
        }

        castSession.sendMessage(CONTROL_NAMESPACE, messageJson.toString())
    }

    private fun sendControlMessage(context: CastContext, action: ControlAction, value: String?) {
        val castSession = context.sessionManager.currentCastSession
        val messageContent = CONTROL_MESSAGE_TEMPLATE.format(action.toString(), value)

        Log.d(TAG, "Sending control message")
        Log.d(TAG, "Namespace: $CONTROL_NAMESPACE")
        Log.d(TAG, "Content: $messageContent")
        if (castSession == null) {
            Log.d(TAG, "Message not sent (no session)")
            return
        }

        castSession.sendMessage(CONTROL_NAMESPACE, messageContent)
    }
}