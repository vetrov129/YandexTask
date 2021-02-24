package hi.dude.yandex.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import hi.dude.yandex.application.App
import java.io.FileNotFoundException
import java.net.URL

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

    constructor(ticker: String, price: String, change: String, company: String, image: ByteArray, currency: String) :
            this(ticker, company, price, true, currency) {
        this.change = change
        this.image = BitmapFactory.decodeByteArray(image, 0, image.size)
    }

    private fun pullImage() {
        val imageUrl = DataFormatter.getImageUrl(ticker) ?: return
        val connection = URL(imageUrl).openConnection()
        connection.doInput = true
        connection.connect()
        try {
            image = BitmapFactory.decodeStream(connection.getInputStream())
        } catch (e: FileNotFoundException) {
            Log.e("StockHolder", "pullImage: not found image $imageUrl")
        }

    }

    fun pullData() {
        val quote = App.connector.getQuote(ticker)
        try {
            change = DataFormatter.getChange(quote?.open, quote?.close, currency)
            price = DataFormatter.addCurrency(quote?.open, currency, true)
        } catch (e: FileNotFoundException) {
            Log.e("StockHolder", "pullData: price not found for $ticker")
        }
        pullImage()
    }

}