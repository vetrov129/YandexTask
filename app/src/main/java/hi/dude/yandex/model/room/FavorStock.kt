package hi.dude.yandex.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favor_stock")
data class FavorStock(
    @PrimaryKey val ticker: String,
    @ColumnInfo(name = "company") val company: String,
    @ColumnInfo(name = "price") val price: String,
    @ColumnInfo(name = "change") val change: String,
    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB) val image: ByteArray,
    @ColumnInfo(name = "currency") val currency: String
)