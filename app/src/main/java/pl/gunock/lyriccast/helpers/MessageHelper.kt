/*
 * Created by Tomasz Kiljanczyk on 4/11/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 9:12 PM
 */

package pl.gunock.lyriccast.helpers

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.enums.ControlAction
import pl.gunock.lyriccast.models.LyricCastSettings

object MessageHelper {
    private const val TAG = "MessageHelper"

    var isBlanked: Boolean = false
        private set

    private var CONTENT_NAMESPACE: String = ""
    private var CONTROL_NAMESPACE: String = ""


    fun initialize(resources: Resources) {
        CONTENT_NAMESPACE = resources.getString(R.string.chromecast_content_namespace)
        CONTROL_NAMESPACE = resources.getString(R.string.chromecast_control_namespace)
    }

    fun sendContentMessage(message: String) {
        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession

        val formattedMessage = message.replace("\n", "<br>")
            .replace("\r", "")

        val contentJson = JSONObject().put("text", formattedMessage)
        val messageContent = contentJson.toString()


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

    fun sendConfiguration(context: Context) {
        val configurationJson = LyricCastSettings(context).getCastConfigurationJson()

        sendControlMessage(
            ControlAction.CONFIGURE,
            configurationJson
        )
    }

    private fun sendControlMessage(action: ControlAction, value: Any) {
        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession

        val messageJson = JSONObject().apply {
            put("action", action.toString())
            put("value", JSONObject.wrap(value))
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

}