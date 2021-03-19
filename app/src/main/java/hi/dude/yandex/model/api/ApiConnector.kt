package hi.dude.yandex.model.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hi.dude.yandex.model.entities.QueryResult
import hi.dude.yandex.model.entities.Quote
import hi.dude.yandex.model.entities.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.net.URL
import java.net.UnknownHostException
// this class (and not Retrofit for example) is used to bypass the limit on the number of requests per day
class ApiConnector {
    private companion object {
        const val TAG = "ApiConnector"

        const val API_URL = "https://financialmodelingprep.com/api/v3"

        var keyIndex = 0
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

        var API_KEY = "apikey=${keyArray[keyIndex]}"

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

    private val gson = Gson()

    private fun updateKey() {
        try {
            keyIndex++
            API_KEY = "apikey=${keyArray[keyIndex]}"
            Log.i(TAG, "updateKey: key ${keyIndex + 1}/${keyArray.size}")
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "updateKey: the keys are out, daily request limit exceeded")
        }
    }


    private suspend fun getJson(request: REQUEST, tickerKey: String?, vararg tokens: Pair<String, String>?): String? {
        while (true) {
            var url = "$API_URL${request.text}"
            if (tickerKey != null) url += "$tickerKey?"
            for (token in tokens) {
                url += token?.first + "=" + token?.second + "&"
            }
            url += API_KEY
            Log.i(TAG, "getJson: $url")
            var json: String? = null

            withContext(Dispatchers.Default) {
                json = try {
                    URL(url).readText()
                } catch (e: UnknownHostException) {
                    Log.e(TAG, "getJson:", e)
                    null
                } catch (e: FileNotFoundException) {
                    delay(1000)  // code 429 Too Many Requests
                    try {
                        URL(url).readText()
                    } catch (e: FileNotFoundException) {
                        Log.e(TAG, "getJson:", e)
                        null
                    }

                }
            }

            if (json == NEED_UPDATE_KEY_RESPONSE)   // key change if the limit is exceeded
                updateKey()
            else
                return json
        }
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
}