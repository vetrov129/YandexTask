package hi.dude.yandex.model.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import hi.dude.yandex.model.entities.WebSocketResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.net.SocketException

class PriceListener(
    val ticker: String,
    val scope: CoroutineScope,
    val updatePrice: suspend (WebSocketResponse?) -> Unit
) : WebSocketListener() {

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
        private const val TAG = "PriceListener"
    }

    private val gson = Gson()
    private val logHandler = CoroutineExceptionHandler { _, exception ->
        println("EXCEPTION/VIEWMODEL: \n${exception.printStackTrace()}}")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("{\"type\":\"subscribe\",\"symbol\":\"$ticker\"}")
        Log.i(TAG, "onOpen: ${response.isSuccessful}")
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        scope.launch(logHandler) { updatePrice(parseMessage(text)) }
        Log.i(TAG, "onMessage: $text")
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        onMessage(webSocket, bytes?.base64())
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        webSocket?.close(NORMAL_CLOSURE_STATUS, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (t is SocketException && t.message == "Socket closed") return
        Log.e(TAG, "onFailure: ", t)
    }

    private fun parseMessage(message: String?): WebSocketResponse? {
        return try {
            gson.fromJson(message, WebSocketResponse::class.java)
        } catch (e: JsonParseException) {
            null
        }
    }
}