package hi.dude.yandex.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface QueryDao {

    @Query("SELECT text FROM user_query ORDER BY date DESC LIMIT :count;")
    fun getStrings(count: Int): List<String>

    @Insert
    fun save(query: UserQuery)

    @Update
    fun updateDate(query: UserQuery)
}