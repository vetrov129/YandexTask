package hi.dude.yandex.model.entities

import com.google.gson.annotations.SerializedName

data class Stock(
    @SerializedName("symbol") val ticker: String,
    @SerializedName("companyName") val company: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("country") val country: String?
)