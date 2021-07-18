/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:19
 */

package pl.gunock.lyriccast.shared.cast

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.LyricCastSettings
import pl.gunock.lyriccast.shared.enums.ControlAction

object CastMessageHelper {
    private const val TAG = "MessageHelper"

    val isBlanked: MutableLiveData<Boolean> = MutableLiveData(false)

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
        isBlanked.postValue(blanked)
        sendControlMessage(ControlAction.BLANK, blanked)
    }

    fun sendConfiguration() {
        val configurationJson = LyricCastSettings.getCastConfigurationJson()

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