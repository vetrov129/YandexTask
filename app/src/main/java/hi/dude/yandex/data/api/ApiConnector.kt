package hi.dude.yandex.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hi.dude.yandex.data.DataFormatter.Companion.deleteOlderThen
import hi.dude.yandex.data.DataFormatter.Companion.fromYear
import hi.dude.yandex.data.DataFormatter.Companion.previousDay
import hi.dude.yandex.data.DataFormatter.Companion.previousMonth
import hi.dude.yandex.data.DataFormatter.Companion.previousSixMonth
import hi.dude.yandex.data.DataFormatter.Companion.previousWeek
import hi.dude.yandex.data.DataFormatter.Companion.toYear
import hi.dude.yandex.data.api.models.*
import java.io.FileNotFoundException
import java.net.URL

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
            CHART_WEEK("/historical-chart/30min/"),         // need tickerKey
            CHART_MONTH("/historical-chart/1hour/"),        // need tickerKey
            CHART_SIX_MONTH("/historical-chart/4hour/"),    // need tickerKey
            CHART_YEAR("/historical-price-full/"),          // need tickerKey
            NEWS("/stock_news?")
        }
    }

    private val gson = Gson()

    private fun updateKey() {
        keyIndex++
        API_KEY = "apikey=${keyArray[keyIndex]}"
        Log.i(TAG, "updateKey: key ${keyIndex + 1}/${keyArray.size}")
    }


    private fun getJson(request: REQUEST, tickerKey: String?, vararg tokens: Pair<String, String>?): String {
        while (true) {
            var url = "$API_URL${request.text}"
            if (tickerKey != null) url += "$tickerKey?"
            for (token in tokens) {
                url += token?.first + "=" + token?.second + "&"
            }
            url += API_KEY
            Log.i(TAG, "getJson: $url")
            val json = URL(url).readText()

            if (json == NEED_UPDATE_KEY_RESPONSE)   // key change if the limit is exceeded
                updateKey()
            else
                return json
        }
    }

    fun getStockList(): ArrayList<Stock> {
        val type = object : TypeToken<ArrayList<Stock?>?>() {}.type
        val json = getJson(REQUEST.TOP_STOCKS, null)
        return gson.fromJson(json, type)
    }

    fun getQuote(ticker: String?): Quote? {
        val type = object : TypeToken<ArrayList<Quote?>?>() {}.type
        val json = getJson(REQUEST.QUOTE, ticker)
        return try {
                (gson.fromJson(json, type) as ArrayList<Quote>)[0]
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    fun getQueryResult(query: String, limit: Int): ArrayList<QueryResult> {
        val type = object : TypeToken<ArrayList<QueryResult?>?>() {}.type
        return try {
            val json = getJson(REQUEST.SEARCH, null, Pair("query", query), Pair("limit", "$limit"))
            gson.fromJson(json, type)
        } catch (e: FileNotFoundException) {
            ArrayList()
        }
    }

    fun getDayChartData(ticker: String): ArrayList<ChartLine> {  // TODO: 25.02.2021 crash JsonSyntaxException
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_DAY, ticker)
        return deleteOlderThen(gson.fromJson(json, type), previousDay())

    }

    fun getWeekChartData(ticker: String): ArrayList<ChartLine> {
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_WEEK, ticker)
        return deleteOlderThen(gson.fromJson(json, type), previousWeek())
    }

    fun getMonthChartData(ticker: String): ArrayList<ChartLine> {
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_MONTH, ticker)
        return deleteOlderThen(gson.fromJson(json, type), previousMonth())
    }

    fun getSixMonthChartData(ticker: String): ArrayList<ChartLine> {
        val type = object : TypeToken<ArrayList<ChartLine?>?>() {}.type
        val json = getJson(REQUEST.CHART_SIX_MONTH, ticker)
        return deleteOlderThen(gson.fromJson(json, type), previousSixMonth())
    }

    fun getYearChartData(ticker: String): ArrayList<ChartLine>? {
        val json = getJson(REQUEST.CHART_YEAR, ticker, fromYear(), toYear())
        return gson.fromJson(json, Historical::class.java).lines
    }

    fun getAllChartData(ticker: String): ArrayList<ChartLine>? {
        val json = getJson(REQUEST.CHART_YEAR, ticker)
        return gson.fromJson(json, Historical::class.java).lines
    }

    fun getSummary(ticker: String): Summary {
        val type = object : TypeToken<ArrayList<Summary?>?>() {}.type
        val json = getJson(REQUEST.SUMMARY, ticker)
        return try {
            (gson.fromJson(json, type) as ArrayList<Summary>)[0]
        } catch (e: IndexOutOfBoundsException) {
            Summary("", "", "", "", "", "")
        }

    }

    fun getNews(ticker: String, limit: Int = 50): ArrayList<NewsItem> {
        val type = object : TypeToken<ArrayList<NewsItem?>?>() {}.type
        val json = getJson(REQUEST.NEWS, null, Pair("tickers", ticker), Pair("limit", "$limit"))
        return gson.fromJson(json, type)
    }
}