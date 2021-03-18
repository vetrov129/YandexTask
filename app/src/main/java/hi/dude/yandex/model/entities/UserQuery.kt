package hi.dude.yandex.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_query")
data class UserQuery(
    @PrimaryKey val text: String,
    @ColumnInfo(name = "date") val date: Long
)