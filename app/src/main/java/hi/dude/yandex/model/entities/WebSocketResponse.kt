package hi.dude.yandex.model.entities

import com.google.gson.annotations.SerializedName

data class WebSocketResponse(
    @SerializedName("type") val type: String?,
    @SerializedName("data") val data: List<PriceData?>?,
) {
    override fun toString(): String {
        return "type = $type data = $data"
    }
}
