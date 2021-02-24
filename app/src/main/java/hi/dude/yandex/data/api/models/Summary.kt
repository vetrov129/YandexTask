package hi.dude.yandex.data.api.models

import com.google.gson.annotations.SerializedName

data class Summary(
    @SerializedName("industry") val industry: String?,
    @SerializedName("website") val website: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("ceo") val ceo: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("ipoDate") val ipoDate: String?
)
