package hi.dude.yandex.model.entities

import com.google.gson.annotations.SerializedName
import hi.dude.yandex.model.entities.ChartLine

data class Historical(
    @SerializedName("historical") val lines: ArrayList<ChartLine>?
)