package hi.dude.yandex.model.entities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.FileNotFoundException
import java.net.URL

data class NewsItem(
    @SerializedName("publishedDate") val date: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("image") val imageUrl: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("site") val site: String?
) {
    var imageBitmap: Bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
    var fullSize = false
}
