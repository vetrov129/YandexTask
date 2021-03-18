package hi.dude.yandex.model.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hi.dude.yandex.model.entities.UserQuery

@Dao
interface QueryDao {

    @Query("SELECT text FROM user_query ORDER BY date DESC LIMIT :count;")
    fun getStrings(count: Int): LiveData<List<String>>

    @Insert
    fun save(query: UserQuery)

    @Update
    fun updateDate(query: UserQuery)
}