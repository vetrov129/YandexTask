package hi.dude.yandex.data.api.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.FileNotFoundException
import java.net.URL

data class NewsItem(
    @SerializedName("publishedDate") val date: String,
    @SerializedName("title") val title: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("text") val text: String,
    @SerializedName("url") val url: String,
    @SerializedName("site") val site: String
) {
    var imageBitmap: Bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
    var fullSize = false

        fun pullImage() {
        val connection = URL(imageUrl).openConnection()
        connection.doInput = true
        connection.connect()
        try {
            imageBitmap = BitmapFactory.decodeStream(connection.getInputStream())
            Log.i("NewsItem", "pullImage: $imageUrl")
        } catch (e: FileNotFoundException) {
            Log.e("NewsItem", "pullImage: not found image $imageUrl")
        }

    }
}
