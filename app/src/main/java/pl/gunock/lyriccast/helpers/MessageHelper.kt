/*
 * Created by Tomasz Kiljanczyk on 4/11/21 7:53 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 7:10 PM
 */

package pl.gunock.lyriccast.helpers

import android.content.res.Resources
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.enums.ControlAction

object MessageHelper {
    private const val TAG = "MessageHelper"

    var isBlanked: Boolean = false
        private set

    private var CONTENT_NAMESPACE: String = ""
    private var CONTROL_NAMESPACE: String = ""
    private var CONTENT_MESSAGE_TEMPLATE: String = ""
    private var CONTROL_MESSAGE_TEMPLATE: String = ""


    fun initialize(resources: Resources) {
        CONTENT_NAMESPACE = resources.getString(R.string.chromecast_content_namespace)
        CONTROL_NAMESPACE = resources.getString(R.string.chromecast_control_namespace)
        CONTENT_MESSAGE_TEMPLATE = resources.getString(R.string.chromecast_content_message_template)
        CONTROL_MESSAGE_TEMPLATE = resources.getString(R.string.chromecast_control_message_template)
    }

    fun sendContentMessage(message: String) {
        val context: CastContext = CastContext.getSharedInstance()!!
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

    fun sendBlank(blanked: Boolean) {
        isBlanked = blanked
        sendControlMessage(ControlAction.BLANK, blanked)
    }

    fun sendControlMessage(action: ControlAction) {
        sendControlMessage(action, null)
    }


    fun sendControlMessage(action: ControlAction, json: JSONObject) {
        val context: CastContext = CastContext.getSharedInstance()!!
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

    private fun sendControlMessage(action: ControlAction, value: Any?) {
        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession
        val messageContent = CONTROL_MESSAGE_TEMPLATE.format(action.toString(), value.toString())

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