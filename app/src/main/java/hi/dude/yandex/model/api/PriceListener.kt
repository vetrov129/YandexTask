package hi.dude.yandex.model.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import hi.dude.yandex.model.entities.WebSocketResponse
import kotlinx.coroutines.*
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.IOException
import java.net.SocketException

class PriceListener(
    val scope: CoroutineScope,
    val updatePrice: suspend (WebSocketResponse?) -> Unit,
    val onWSOpen: () -> Unit = {}
) : WebSocketListener() {

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
        private const val TAG = "PriceListener"
    }

    private val gson = Gson()
    private lateinit var webSocket: WebSocket

    private val logHandler = CoroutineExceptionHandler { _, exception ->
        println("EXCEPTION/VIEWMODEL: \n${exception.printStackTrace()}}")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        onWSOpen()
        Log.i(TAG, "onOpen: ${response.isSuccessful}")
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        scope.launch(logHandler) { updatePrice(parseMessage(text)) }
//        Log.i(TAG, "onMessage: $text")
//        if (!scope.isActive) onClosing(webSocket, NORMAL_CLOSURE_STATUS, null)
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        onMessage(webSocket, bytes?.base64())
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        Log.i(TAG, "onClosing: ")
        webSocket?.close(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (t is SocketException && t.message == "Socket closed") return
        Log.e(TAG, "onFailure: ", t)
    }

    private fun parseMessage(message: String?): WebSocketResponse? {
        return try {
            val obj = gson.fromJson(message, WebSocketResponse::class.java)
//            Log.i(TAG, "parseMessage: $obj")
            obj
        } catch (e: JsonParseException) {
            null
        }
    }

    fun subscribe(tickers: Array<String>) {
        tickers.forEach {
            try {
                scope.launch(logHandler) { webSocket.send("{\"type\":\"subscribe\",\"symbol\":\"$it\"}") }
            } catch (e: IOException) {
                Log.e(TAG, "subscribe: IOException ${e.message}")
            }
        }
    }

    fun unsubscribe(tickers: Array<String>) {
        tickers.forEach {
            try {
                scope.launch(logHandler) { webSocket.send("{\"type\":\"unsubscribe\",\"symbol\":\"$it\"}") }
            } catch (e: IOException) {
                Log.e(TAG, "subscribe: IOException ${e.message}")
            }
        }
    }
}