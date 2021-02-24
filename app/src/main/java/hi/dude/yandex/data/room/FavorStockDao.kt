package hi.dude.yandex.data.room

import androidx.room.*

@Dao
interface FavorStockDao {

    @Query("SELECT * FROM favor_stock;")
    fun getAll(): List<FavorStock>

    @Insert
    fun save(stock: FavorStock)

    @Update
    fun update(stock: FavorStock)

    @Delete
    fun delete(stock: FavorStock)
}