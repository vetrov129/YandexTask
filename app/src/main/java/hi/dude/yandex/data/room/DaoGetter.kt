package hi.dude.yandex.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavorStock::class, UserQuery::class], version = 5)
abstract class DaoGetter: RoomDatabase() {
    abstract fun getFavorStockDao(): FavorStockDao
    abstract fun getQueryDao(): QueryDao
}