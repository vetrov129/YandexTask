package hi.dude.yandex.viewmodel

import android.graphics.Bitmap
import android.util.Log
import com.squareup.picasso.Picasso
import hi.dude.yandex.R
import hi.dude.yandex.model.Repository
import hi.dude.yandex.model.entities.Stock
import hi.dude.yandex.model.entities.FavorStock
import hi.dude.yandex.model.entities.QueryResult
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class StockHolder(
    val ticker: String,
    companyOrNull: String?,
    priceOrNull: String?,
    currencyOrNull: String?
) {
    var image: Bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
    var change = ""
    val currency = currencyOrNull ?: "USD"
    val company = DataFormatter.cutCompany(companyOrNull) ?: ""
    var price = priceOrNull ?: ""
    var priceClose: Double? = 0.0
    var priceDouble: Double? = 0.0

    constructor(stock: Stock) : this(
        stock.ticker,
        DataFormatter.cutCompany(stock.company),
        DataFormatter.addCurrency(stock.price, stock.country, true),
        stock.country
    )

    constructor(favor: FavorStock): this(favor.ticker, favor.company, favor.price, favor.currency)

    constructor(result: QueryResult): this(result.ticker, result.company, null, result.currency)

    fun toFavor(): FavorStock {
        val bos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, bos)
        val array = bos.toByteArray()
        return FavorStock(ticker, company, price, change, array, currency)
    }

    suspend fun pullImage(logMessage: Any? = null) {
        val imageUrl = "https://financialmodelingprep.com/image-stock/$ticker"
        val result = CoroutineScope(Dispatchers.IO).async {
            try {
                Picasso.get()
                    .load("$imageUrl.jpg")
                    .error(R.drawable.empty)
                    .placeholder(R.drawable.empty)
                    .get()
            } catch (e: IOException) {
                try {
                    Picasso.get()
                        .load("$imageUrl.png")
                        .error(R.drawable.empty)
                        .placeholder(R.drawable.empty)
                        .get()
                } catch (e: IOException) {
                    Log.e("StockHolder", "pullImage: $logMessage", e)
                }
            }
        }
        image = result.await() as Bitmap
    }

    suspend fun pullChangeAndPrice(logMessage: Any? = null) {
        val result = CoroutineScope(Dispatchers.IO).async {
            try {
                Repository.getInstance().getQuote(ticker)
            } catch (e: IOException) {
                Log.e("StockHolder", "pullChange: $logMessage", e)
                null
            }
        }
        val quote = result.await()

        change = DataFormatter.getChange(quote?.priceDouble, quote?.close, currency)
        priceClose = quote?.close
        priceDouble = quote?.priceDouble
        if (quote != null)
         price = DataFormatter.addCurrency(quote.priceDouble, currency, true)
    }
}