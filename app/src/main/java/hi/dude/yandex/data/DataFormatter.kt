package hi.dude.yandex.data

import android.graphics.Bitmap
import android.util.Log
import hi.dude.yandex.application.App
import hi.dude.yandex.data.api.models.ChartLine
import hi.dude.yandex.data.api.models.QueryResult
import hi.dude.yandex.data.api.models.Stock
import hi.dude.yandex.data.room.FavorStock
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class DataFormatter {

    companion object {
        private const val IMAGE_PATH = "https://financialmodelingprep.com/image-stock/"

        private val EUR_COUNTRIES = arrayOf(
            "AT", "BE", "DE", "DK", "IT", "IE", "ES", "LU", "NL", "PT",
            "FI", "FR", "GR", "SI", "CY", "MT", "SK", "EE", "LV", "LT"
        )

        fun getChange(new: Double, old: Double, currency: String): String {
            var result = ""
            result += if (new - old < 0) "-" else "+"
            result += addCurrency(new - old, currency, false)
            var percentStr = String.format("%.2f", abs((new - old) / new * 100)) + "%"
            result += " ($percentStr)"
            return result
        }

        fun addCurrency(volume: Double, currency: String, long: Boolean): String {
            var result = ""
            if (currency == "USD" || currency == "US") result += "$"
            result +=
                if (long) volume.toBigDecimal().abs().toPlainString()
                else String.format("%.2f", volume.toBigDecimal().abs())
            if (currency == "CNY" || currency == "CN") result += " \u00A5"
            if (currency == "EUR" || currency in EUR_COUNTRIES) result += " \u20AC"
            if (currency == "RUB" || currency == "RU") result += " \u20BD"
            return result
        }

        private fun cutCompany(company: String?): String? {
            return if (company != null && company.length > 24) company.substring(0, 24) + "..." else company
        }

        fun companyToQuery(company: String?): String  {
            if (company == null) return ""
            return company.split(Regex("(\\h|\\.)"))[0]
        }

        fun getImageUrl(ticker: String?): String? {
            return if (ticker == null) null else "$IMAGE_PATH$ticker.jpg"
        }

        fun getStockHolders(stocks: List<Stock>, start: Int = 0, count: Int = 20): ArrayList<StockHolder> {
            val holders = ArrayList<StockHolder>()
            try {
                for (i in start until (count + start)) {
                    holders.add(
                        StockHolder(
                            stocks[i].ticker,
                            cutCompany(stocks[i].company),
                            addCurrency(stocks[i].price, stocks[i].country, true),
                            App.checkIsFavor(stocks[i].ticker),
                            stocks[i].country
                        )
                    )
                }
            } catch (e: IndexOutOfBoundsException) {
                Log.e("DataFormatter", "getStockHolders: ", e)
            }

            return holders
        }

        fun getStockHolders(stocks: List<FavorStock>): ArrayList<StockHolder> {
            val holders = ArrayList<StockHolder>()
            for (stock in stocks) {
                holders.add(
                    StockHolder(
                        stock.ticker,
                        stock.price,
                        stock.change,
                        stock.company,
                        stock.image,
                        stock.currency
                    )
                )
            }
            return holders
        }

        fun getStockHolders(queryResults: ArrayList<QueryResult>): ArrayList<StockHolder> {
            val holders = ArrayList<StockHolder>()
            for (item in queryResults) {
                holders.add(
                    StockHolder(
                        item.ticker,
                        cutCompany(item.company),
                        "",
                        App.checkIsFavor(item.ticker),
                        item.currency
                    )
                )
            }
            return holders
        }

        fun holderToFavor(holder: StockHolder): FavorStock {
            val bos = ByteArrayOutputStream()
            holder.image.compress(Bitmap.CompressFormat.PNG, 100, bos)
            return FavorStock(
                holder.ticker,
                holder.company,
                holder.price,
                holder.change,
                bos.toByteArray(),
                holder.currency
            )
        }

        fun fromYear(): Pair<String, String> = Pair("from", (LocalDate.now() - Period.of(1, 0, 0)).toString())

        fun toYear(): Pair<String, String> = Pair("to", LocalDate.now().toString())

        fun previousDay(): LocalDateTime = LocalDateTime.now() - Period.of(0, 0, 1)

        fun previousWeek(): LocalDateTime = LocalDateTime.now() - Period.of(0, 0, 7)

        fun previousMonth(): LocalDateTime = LocalDateTime.now() - Period.of(0, 1, 0)

        fun previousSixMonth(): LocalDateTime = LocalDateTime.now() - Period.of(0, 6, 0)

        fun deleteOlderThen(array: ArrayList<ChartLine>, date: LocalDateTime): ArrayList<ChartLine> {
            array.removeIf { LocalDateTime.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) < date }
            return array
        }

        fun toPrettyDate(date: String): String {
            return if (date.length > 10) {
                val ldate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ldate.format(DateTimeFormatter.ofPattern("HH:mm d MMM yyyy", Locale.ENGLISH)).toLowerCase()
            } else {
                val ldate = LocalDate.parse(date)
                ldate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)).toLowerCase()
            }
        }

        fun getCountryByCode(code: String): String {
            return when (code) {
                "US" -> "United States ($code)"
                "CN" -> "China ($code)"
                "NL" -> "Netherlands ($code)"
                "RU" -> "Russia ($code)"
                else -> code
            }
        }
    }
}