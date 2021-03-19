package hi.dude.yandex.model.room

import androidx.room.*
import hi.dude.yandex.model.entities.FavorStock

@Dao
interface StockDao {

    @Query("SELECT * FROM favor_stock;")
    fun getAll(): List<FavorStock>

    @Insert
    fun save(stock: FavorStock)

    @Update
    fun update(stock: FavorStock)

    @Delete
    fun delete(stock: FavorStock)
}