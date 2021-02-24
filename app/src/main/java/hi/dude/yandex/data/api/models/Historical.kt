package hi.dude.yandex.data.api.models

import com.google.gson.annotations.SerializedName

data class Historical(
    @SerializedName("historical") val lines: ArrayList<ChartLine>?
)