package hi.dude.yandex.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hi.dude.yandex.model.entities.Stock
import hi.dude.yandex.model.entities.UserQuery
import hi.dude.yandex.viewmodel.StockHolder

@Database(entities = [FavorStock::class, UserQuery::class], version = 2)
abstract class DaoGetter: RoomDatabase() {
    abstract fun getStockDao(): StockDao
    abstract fun getQueryDao(): QueryDao
}