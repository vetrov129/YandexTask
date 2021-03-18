package hi.dude.yandex.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import hi.dude.yandex.model.api.ApiConnector
import hi.dude.yandex.model.entities.Stock
import hi.dude.yandex.model.room.DaoGetter
import hi.dude.yandex.model.room.QueryDao
import hi.dude.yandex.model.room.StockDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(val app: Application) {

    private val TAG = "Repository"

    private val connector = ApiConnector()
    private lateinit var favorDao: StockDao
    private lateinit var queryDao: QueryDao

    lateinit var allStocks: ArrayList<Stock>

//    val stocks = MutableLiveData<ArrayList<Stock>>()
    lateinit var favors: LiveData<List<Stock>>
        private set

    suspend fun initDao() = withContext(Dispatchers.IO) {
        val daoGetter = Room.databaseBuilder(app.applicationContext, DaoGetter::class.java, "stocks.sqlite")
            .fallbackToDestructiveMigration()  // TODO: 18.03.2021 remove it!!!
            .build()

        favorDao = daoGetter.getStockDao()
        queryDao = daoGetter.getQueryDao()
    }

    suspend fun pullStocks() {
        try {
            allStocks = connector.getAllStocks()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun pullFavors() {
        favors = favorDao.getAll()
    }


}