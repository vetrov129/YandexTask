package hi.dude.yandex.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "favor_stock")
data class Stock(
    @PrimaryKey @SerializedName("symbol") val ticker: String,
    @ColumnInfo(name = "company") @SerializedName("companyName") val company: String?,
    @ColumnInfo(name = "price") @SerializedName("price") val price: Double?,
    @ColumnInfo(name = "country") @SerializedName("country") val country: String?
)