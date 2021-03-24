package hi.dude.yandex.model.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hi.dude.yandex.model.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.FileNotFoundException
import java.net.URL
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.Period
import java.util.*
import kotlin.collections.ArrayList

// this class (and not Retrofit for example) is used to bypass the limit on the number of requests per day
class ApiConnector {
    private companion object {
        const val TAG = "ApiConnector"

        const val API_URL = "https://financialmodelingprep.com/api/v3"

        val keyArray = arrayOf(
            "60ee277f0cde9daa7705f6a79b1f47ba",
            "5b19fce62961a4207f6b574e47f242ba",
            "1a08f439ead54166d7afc86b8149839d",
            "97a0cffc3a6f0bca3301be8ee03bdd08",
            "3ac432c2c30f4b55286659994e6e1112",
            "fd73c0b67949e7f5356c67d219767ab3",
            "85bb050ff332eef4897637accb7c9e04",
            "5812ce3abf6b7718aeb489ffc288f065",
            "c24557db59bf6b80dd54cc86dacbc9ce",
            "9630f8328efd9f2ded97a3592ac6c761",
            "937ea76342f0a99eb456166a11001dc3",
            "3e8cf3f8e72f60540491227b12b07264",
            "942f073828ccda72d278aa608cd3c53e",
            "aaa623a4835fc8826c255ffd1d375e9c",
            "1862c28f578ec3bac1f1f67250d88471",
            "f27c99c0997da9960bded9d03e5b17dc",
            "91c2b5a8d7aecf7c27b92116f98978df",
            "1ebc3f616be306abbee6ea63bbf8d165",
            "709bab142f28fc5c97c1a358a547c0eb",
            "dbe960a0f368d06c49f2748aa5bcd642",
            "6b574ce345b0fe3370403ac4ae9c07b2"
        )

        const val NEED_UPDATE_KEY_RESPONSE =
            "{\"Error Message\" : \"Limit Reach . " +
                    "Please upgrade your plan or visit our " +
                    "documentation for more details at " +
                    "https://financialmodelingprep.com/developer/docs/pricing \"}"

        enum class REQUEST(val text: String) {
            QUOTE("/quote/"),                               // need tickerKey
            SEARCH("/search?exchange=NASDAQ&"),
            SUMMARY("/profile/"),                           // need tickerKey
            TOP_STOCKS("/stock-screener?exchange=nasdaq&"),
            CHART_DAY("/historical-chart/5min/"),           // need tickerKey
            CHART_WEEK("/historical-chart/15min/"),         // need tickerKey
            CHART_MONTH("/historical-chart/1hour/"),        // need tickerKey
            CHART_SIX_MONTH("/historical-chart/4hour/"),    // need tickerKey
            CHART_YEAR("/historical-price-full/"),          // need tickerKey
            NEWS("/stock_news?")
        }
    }

    private val keys = LinkedList<String>()

    init {
        keys.addAll(keyArray)
    }

    private val gson = Gson()
    private lateinit var webSocket: WebSocket

    @JvmName("getJson1")
    private suspend fun getJson(request: REQUEST, tickerKey: String?, vararg tokens: Pair<String, String>?): String? {
        return getJson(request, tickerKey, tokens)
    }

    private suspend fun getJson(
        request: REQUEST,
        tickerKey: String?,
        tokens: Array<out Pair<String, String>?>
    ): String? = withContext(Dispatchers.Default) {
        var currentKey = keys.first
        while (true) {
            val url = buildUrl(request, tickerKey, currentKey, tokens)
            val json: String? = try {
                URL(url).readText()
            } catch (e: UnknownHostException) { // waiting for the connection to be restored
                Log.i(TAG, "getJson: UnknownHostException, waiting for the connection")
                delay(2500)
                getJson(request, tickerKey, tokens)
            } catch (e: FileNotFoundException) { // code 429 Too Many Requests
                Log.i(TAG, "getJson: FileNotFoundException, try again")
                delay(1000)
                getJson(request, tickerKey, tokens)
            }
            if (json == NEED_UPDATE_KEY_RESPONSE) {
                keys.remove(currentKey)
                if (keys.size == 0) {
                    Log.e(TAG, "getJson: the keys are out, daily request limit exceeded")
                    return@withContext null
                }
                currentKey = keys.first
            } else {
                return@withContext json
            }
        }
        null
    }

    private fun buildUrl(
        request: REQUEST,
        tickerKey: String?,
        key: String,
        tokens: Array<out Pair<String, String>?>
    ): String {
        var url = "$API_URL${request.text}"
        if (tickerKey != null) url += "$tickerKey?"
        for (token in tokens) {
            url += token?.first + "=" + token?.second + "&"
        }
        url += "apikey=$key"
        return url
    }

    suspend fun getAllStocks(): ArrayList<Stock> {
        val type = object : TypeToken<ArrayList<Stock?>?>() {}.type
        val json = getJson(REQUEST.TOP_STOCKS, null)
        return gson.fromJson(json, type) ?: ArrayList()
    }

    suspend fun getQuote(ticker: String?): Quote? {
        val type = object : TypeToken<ArrayList<Quote?>?>() {}.type
        val json = getJson(REQUEST.QUOTE, ticker)
        return try {
            (gson.fromJson(json, type) as ArrayList<Quote>)[0]
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    suspend fun getQueryResult(query: String, limit: Int): ArrayList<QueryResult> {
        val type = object : TypeToken<ArrayList<QueryResult?>?>() {}.type
        return try {
            val json = getJson(REQUEST.SEARCH, null, Pair("query", query), Pair("limit", "$limit"))
            gson.fromJson(json, type) ?: ArrayList()
        } catch (e: FileNotFoundException) {
            ArrayList()
        }
    }

    suspend fun getDayChartData(ticker: String): ArrayList<ChartLine> {
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_DAY, ticker)
        return gson.fromJson(json, type) ?: ArrayList()
    }

    suspend fun getWeekChartData(ticker: String): ArrayList<ChartLine> {
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_WEEK, ticker)
        return gson.fromJson(json, type) ?: ArrayList()
    }

    suspend fun getMonthChartData(ticker: String): ArrayList<ChartLine> {
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_MONTH, ticker)
        return gson.fromJson(json, type) ?: ArrayList()
    }

    suspend fun getSixMonthChartData(ticker: String): ArrayList<ChartLine> {
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_SIX_MONTH, ticker)
        return gson.fromJson(json, type) ?: ArrayList()
    }

    suspend fun getYearChartData(ticker: String): ArrayList<ChartLine>? {
        val json = getJson(REQUEST.CHART_YEAR, ticker, fromYear(), toYear())
        return gson.fromJson(json, Historical::class.java)?.lines
    }

    suspend fun getAllTimeChartData(ticker: String): ArrayList<ChartLine>? {
        val json = getJson(REQUEST.CHART_YEAR, ticker)
        return gson.fromJson(json, Historical::class.java)?.lines
    }

    private fun fromYear(): Pair<String, String> = Pair("from", (LocalDate.now() - Period.ofYears(1)).toString())

    private fun toYear(): Pair<String, String> = Pair("to", LocalDate.now().toString())

    suspend fun getSummary(ticker: String): Summary? {
        val type = object : TypeToken<ArrayList<Summary?>?>() {}.type
        val json = getJson(REQUEST.SUMMARY, ticker)
        return try {
            (gson.fromJson(json, type) as ArrayList<Summary>)[0]
        } catch (e: IndexOutOfBoundsException) {
            null
        } catch (e: NullPointerException) {
            null
        }
    }

    suspend fun getNews(ticker: String, limit: Int = 20): ArrayList<NewsItem> {
        val type = object : TypeToken<ArrayList<NewsItem?>?>() {}.type
        val json = getJson(REQUEST.NEWS, null, Pair("tickers", ticker), Pair("limit", "$limit"))
        return gson.fromJson(json, type) ?: ArrayList()
    }

    suspend fun openWebsocket(
        ticker: String,
        scope: CoroutineScope,
        updatePrice: suspend (WebSocketResponse?) -> Unit
    ) = withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .build()
            val request = Request.Builder()
                .url("wss://ws.finnhub.io?token=c1d746v48v6p64720gqg")
                .build()

            val wsListener = PriceListener(ticker, scope, updatePrice)
            webSocket = client.newWebSocket(request, wsListener)
        }

    suspend fun closeWebSocket() = withContext(Dispatchers.IO) {
        webSocket.cancel()
    }
}