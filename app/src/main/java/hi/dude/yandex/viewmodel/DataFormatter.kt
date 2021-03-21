package hi.dude.yandex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import hi.dude.yandex.model.entities.ChartLine
import hi.dude.yandex.model.entities.Stock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

        fun getChange(new: Double?, old: Double?, currency: String?): String {
            if (new == null || old == null || currency == null) return ""
            if (new == 0.0) return "+${addCurrency(new, currency, false)} (0.0%)"
            var result = ""
            result += if (new - old < 0) "-" else "+"
            result += addCurrency(new - old, currency, false)
            var percentStr = String.format("%.2f", abs((new - old) / new * 100)) + "%"
            result += " ($percentStr)"
            return result
        }

        fun addCurrency(volume: Double?, currency: String?, long: Boolean): String {
            if (volume == null) return ""
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

        fun cutCompany(company: String?): String? {
            return if (company != null && company.length > 24) company.substring(0, 24) + "..." else company
        }

        fun companyToQuery(company: String?): String  {
            if (company == null) return ""
            return company.split(Regex("(\\h|\\.)"))[0]
        }

        fun getImageUrl(ticker: String?): String? {
            return if (ticker == null) null else "$IMAGE_PATH$ticker.jpg"
        }

        fun stocksToHolders(stocks: LiveData<List<Stock>>): MutableLiveData<ArrayList<StockHolder>> {
            val holders = MutableLiveData<ArrayList<StockHolder>>()
            holders.value = ArrayList()
            if (stocks.value == null) return holders
            for (stock in stocks.value!!) {
                holders.value?.add(StockHolder(stock))
            }
            return holders
        }

        private fun deleteOlderThen(array: ArrayList<ChartLine>?, date: LocalDateTime): ArrayList<ChartLine> {
            if (array == null) return ArrayList()
            array.removeIf {
                LocalDateTime.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) < date
            }
            return array
        }

        fun toPrettyDate(date: String?): String {
            if (date == null) return ""
            return try {
                if (date.length > 10) {
                    val ldate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    ldate.format(DateTimeFormatter.ofPattern("HH:mm d MMM yyyy", Locale.ENGLISH)).toLowerCase()
                } else {
                    val ldate = LocalDate.parse(date)
                    ldate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)).toLowerCase()
                }
            } catch (e: DateTimeParseException) {
                return ""
            }
        }

        fun getCountryByCode(code: String?): String {
            return when (code) {
                "US" -> "United States ($code)"
                "CN" -> "China ($code)"
                "NL" -> "Netherlands ($code)"
                "RU" -> "Russia ($code)"
                null -> ""
                else -> code
            }
        }

        fun cutDescription(des: String?): String {
            if (des == null) return ""

            return if (des.length > 400)
                des.substring(0, 400) + "..."
            else des
        }
    }
}