package hi.dude.yandex.model.entities

import com.google.gson.annotations.SerializedName

data class PriceData(
    @SerializedName("p") val price: Double?,
    @SerializedName("t") val timestamp: Long?,
    @SerializedName("s") val ticker: String?,
) {
    override fun toString(): String {
        return "p = $price s = $ticker t = $timestamp"
    }
}
