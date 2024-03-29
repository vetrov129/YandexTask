package hi.dude.yandex.model.entities

import com.google.gson.annotations.SerializedName

data class ChartLine(
    @SerializedName("date") val date: String?,
    @SerializedName("close") val price: Double?
)