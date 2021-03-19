package hi.dude.yandex.model.room

import androidx.lifecycle.LiveData
import androidx.room.*
import hi.dude.yandex.model.entities.Stock

@Dao
interface StockDao {

    @Query("SELECT * FROM favor_stock;")
    fun getAll(): LiveData<List<Stock>>

    @Insert
    fun save(stock: Stock)

    @Update
    fun update(stock: Stock)

    @Delete
    fun delete(stock: Stock)
}