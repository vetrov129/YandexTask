package hi.dude.yandex.model.room

import androidx.lifecycle.LiveData
import androidx.room.*
import hi.dude.yandex.model.entities.Stock

@Dao
interface StockDao {

    @Query("SELECT * FROM favor_stock;")
    suspend fun getAll(): LiveData<List<Stock>>

    @Insert
    suspend fun save(stock: Stock)

    @Update
    suspend fun update(stock: Stock)

    @Delete
    suspend fun delete(stock: Stock)
}