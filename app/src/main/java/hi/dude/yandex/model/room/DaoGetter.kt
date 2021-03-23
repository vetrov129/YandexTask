package hi.dude.yandex.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import hi.dude.yandex.model.entities.FavorStock
import hi.dude.yandex.model.entities.UserQuery

@Database(entities = [FavorStock::class, UserQuery::class], version = 4)
abstract class DaoGetter: RoomDatabase() {
    abstract fun getStockDao(): StockDao
    abstract fun getQueryDao(): QueryDao
}