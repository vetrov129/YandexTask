package hi.dude.yandex.model.entities

import com.google.gson.annotations.SerializedName

data class Quote(
    @SerializedName("name") val company: String?,
    @SerializedName("open") val open: Double?,
    @SerializedName("previousClose") val close: Double?,
    @SerializedName("change") val change: Double?,
    @SerializedName("changesPercentage") val percent: Double?
)