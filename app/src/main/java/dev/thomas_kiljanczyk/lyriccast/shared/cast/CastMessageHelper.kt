/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.shared.cast

import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import dev.thomas_kiljanczyk.lyriccast.shared.enums.ControlAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

object CastMessageHelper {
    private const val TAG = "MessageHelper"
    private const val CONTENT_NAMESPACE: String = "urn:x-cast:lyric.cast.content"
    private const val CONTROL_NAMESPACE: String = "urn:x-cast:lyric.cast.control"

    private val _isBlanked: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isBlanked: StateFlow<Boolean> get() = _isBlanked

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
        if (isNotInSession()) {
            return
        }

        _isBlanked.value = blanked
        sendControlMessage(ControlAction.BLANK, blanked)
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

    private fun isNotInSession(): Boolean {
        val castSession = runBlocking(Dispatchers.Main) {
            val context = CastContext.getSharedInstance()
            context?.sessionManager?.currentCastSession
        }

        return castSession == null
    }

    private fun sendControlMessage(action: ControlAction, value: Any) {
        val messageJson = JSONObject().apply {
            put("action", action.toString())
            put("value", JSONObject.wrap(value))
        }

        Log.d(TAG, "Sending control message")
        Log.d(TAG, "Namespace: $CONTROL_NAMESPACE")
        Log.d(TAG, "Content: $messageJson")
        if (isNotInSession()) {
            Log.d(TAG, "Message not sent (no session)")
            return
        }

        val context: CastContext = CastContext.getSharedInstance()!!
        val castSession = context.sessionManager.currentCastSession!!
        castSession.sendMessage(CONTROL_NAMESPACE, messageJson.toString())
    }

}