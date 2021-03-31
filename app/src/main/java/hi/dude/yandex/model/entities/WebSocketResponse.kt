package hi.dude.yandex.model.entities

import com.google.gson.annotations.SerializedName

data class WebSocketResponse(
    @SerializedName("type") val type: String?,
    @SerializedName("data") var data: ArrayList<PriceData?>?,
) {
    override fun toString(): String {
        return "type = $type data = $data"
    }
}
