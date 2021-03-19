package hi.dude.yandex.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import hi.dude.yandex.model.api.ApiConnector
import hi.dude.yandex.model.entities.Quote
import hi.dude.yandex.model.entities.Stock
import hi.dude.yandex.model.room.DaoGetter
import hi.dude.yandex.model.room.FavorStock
import hi.dude.yandex.model.room.QueryDao
import hi.dude.yandex.model.room.StockDao
import hi.dude.yandex.viewmodel.StockHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository private constructor() {

    companion object {
        private var repository: Repository? = null

        fun getInstance(): Repository {
            if (repository == null)
                repository = Repository()
            return repository!!
        }
    }

    private val connector = ApiConnector()
    private lateinit var favorDao: StockDao
    private lateinit var queryDao: QueryDao

    val allStocks: MutableLiveData<ArrayList<Stock>> = MutableLiveData()
    var favors: MutableLiveData<List<FavorStock>> = MutableLiveData()
        private set

    suspend fun initDao(app: Application) = withContext(Dispatchers.IO) {
        val daoGetter = Room.databaseBuilder(app.applicationContext, DaoGetter::class.java, "stocks.sqlite")
            .fallbackToDestructiveMigration()
            .build()

        favorDao = daoGetter.getStockDao()
        queryDao = daoGetter.getQueryDao()
    }

    suspend fun pullAllStocks() {
        try {
            allStocks.value = connector.getAllStocks()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun pullFavors() = withContext(Dispatchers.IO){
        val list = favorDao.getAll()
        withContext(Dispatchers.Main) { favors.value = list }
    }

    suspend fun getQuote(ticker: String): Quote? {
        return connector.getQuote(ticker)
    }

    suspend fun deleteFavor(favor: FavorStock) = withContext(Dispatchers.IO) {
        favorDao.delete(favor)
    }

    suspend fun saveFavor(favor: FavorStock) = withContext(Dispatchers.IO) {
        favorDao.save(favor)
    }
}

