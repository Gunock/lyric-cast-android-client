/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 18:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 18:15
 */

package pl.gunock.lyriccast.shared.cast

import android.content.res.Resources
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.shared.enums.ControlAction

object CastMessageHelper {
    private const val TAG = "MessageHelper"

    val isBlanked: StateFlow<Boolean> get() = _isBlanked
    private val _isBlanked: MutableStateFlow<Boolean> = MutableStateFlow(true)

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
        if (isInSession()) {
            _isBlanked.value = blanked
            sendControlMessage(ControlAction.BLANK, blanked)
        }
    }

    fun sendConfiguration(configurationJson: JSONObject) {
        sendControlMessage(
            ControlAction.CONFIGURE,
            configurationJson
        )
    }

    fun onSessionEnded() {
        _isBlanked.value = true
    }

    private fun isInSession(): Boolean {
        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession
        return castSession != null
    }

    private fun sendControlMessage(action: ControlAction, value: Any) {
        val messageJson = JSONObject().apply {
            put("action", action.toString())
            put("value", JSONObject.wrap(value))
        }

        Log.d(TAG, "Sending control message")
        Log.d(TAG, "Namespace: $CONTROL_NAMESPACE")
        Log.d(TAG, "Content: $messageJson")
        if (!isInSession()) {
            Log.d(TAG, "Message not sent (no session)")
            return
        }

        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession!!
        castSession.sendMessage(CONTROL_NAMESPACE, messageJson.toString())
    }

}