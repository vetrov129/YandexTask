package hi.dude.yandex.data.api.models

import com.google.gson.annotations.SerializedName

data class QueryResult(
    @SerializedName("symbol") val ticker: String,
    @SerializedName("name") val company: String,
    @SerializedName("currency") val currency: String
)