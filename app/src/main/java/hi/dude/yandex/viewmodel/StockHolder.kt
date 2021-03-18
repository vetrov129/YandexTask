package hi.dude.yandex.viewmodel

import android.graphics.Bitmap
import android.util.Log
import com.squareup.picasso.Picasso
import hi.dude.yandex.R
import hi.dude.yandex.model.entities.Quote
import hi.dude.yandex.model.entities.Stock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.IOException

class StockHolder(
    val ticker: String,
    companyOrNull: String?,
    priceOrNull: String?,
    var isFavor: Boolean,
    currencyOrNull: String?
) {
    var image: Bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
    var change = ""
    val currency = currencyOrNull ?: "USD"
    val company = companyOrNull ?: ""
    var price = priceOrNull ?: ""

    private val imageUrl = "https://financialmodelingprep.com/image-stock/$ticker"

    constructor(stock: Stock) : this(
        stock.ticker,
        DataFormatter.cutCompany(stock.company),
        DataFormatter.addCurrency(stock.price, stock.country, true),
        false,
        stock.country
    )

    private suspend fun pullImage(logMessage: Any? = null) {
        val result = CoroutineScope(Dispatchers.IO).async {
            try {
                Picasso.get()
                    .load(imageUrl)
                    .centerCrop()
                    .error(R.drawable.empty)
                    .placeholder(R.drawable.empty)
                    .get()
            } catch (e: IOException) {
                Log.e("StockHolder", "pullImage: $logMessage", e)
            }
        }
        image = result.await() as Bitmap
    }

    private suspend fun pullChangeAndPrice(logMessage: Any? = null) {
        val result = CoroutineScope(Dispatchers.IO).async {
            try {

            } catch (e: IOException) {
                Log.e("StockHolder", "pullChange: $logMessage", e)
            }
        }
        val quote = result.await() as Quote

        change = DataFormatter.getChange(quote.open, quote.close, currency)
        price = DataFormatter.addCurrency(quote.open, currency, true)
    }

    suspend fun pullData(logMessage: Any? = null) {
        pullImage(logMessage)
        pullChangeAndPrice(logMessage)
    }
}