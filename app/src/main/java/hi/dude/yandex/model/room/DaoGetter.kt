package hi.dude.yandex.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import hi.dude.yandex.model.entities.Stock
import hi.dude.yandex.model.entities.UserQuery

@Database(entities = [Stock::class, UserQuery::class], version = 1)
abstract class DaoGetter: RoomDatabase() {
    abstract fun getStockDao(): StockDao
    abstract fun getQueryDao(): QueryDao
}