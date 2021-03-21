package hi.dude.yandex.model

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import hi.dude.yandex.model.api.ApiConnector
import hi.dude.yandex.model.entities.*
import hi.dude.yandex.model.room.DaoGetter
import hi.dude.yandex.model.room.QueryDao
import hi.dude.yandex.model.room.StockDao
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

    val favorTickerSet = HashSet<String>()

    // list data
    val allStocks: MutableLiveData<ArrayList<Stock>> = MutableLiveData()
    var favors: MutableLiveData<List<FavorStock>> = MutableLiveData()
    val searchedQueries: MutableLiveData<List<String>> = MutableLiveData()
    val queryResults: MutableLiveData<ArrayList<QueryResult>> = MutableLiveData()

    // card data
    val dayChart: MutableLiveData<ArrayList<ChartLine>> = MutableLiveData()
    val weekChart: MutableLiveData<ArrayList<ChartLine>> = MutableLiveData()
    val monthChart: MutableLiveData<ArrayList<ChartLine>> = MutableLiveData()
    val sixMonthChart: MutableLiveData<ArrayList<ChartLine>> = MutableLiveData()
    val yearChart: MutableLiveData<ArrayList<ChartLine>> = MutableLiveData()
    val allTimeChart: MutableLiveData<ArrayList<ChartLine>> = MutableLiveData()
    val summary: MutableLiveData<Summary> = MutableLiveData()
    val news: MutableLiveData<ArrayList<NewsItem>> = MutableLiveData()

    suspend fun init(app: Application) = withContext(Dispatchers.IO) {
        val daoGetter = Room.databaseBuilder(app.applicationContext, DaoGetter::class.java, "stocks.sqlite")
            .fallbackToDestructiveMigration()
            .build()

        favorDao = daoGetter.getStockDao()
        queryDao = daoGetter.getQueryDao()
    }

    fun fillFavorSet() {
        favors.value?.forEach { favorTickerSet.add(it.ticker) }
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
        favorTickerSet.remove(favor.ticker)
        favorDao.delete(favor)
    }

    suspend fun saveFavor(favor: FavorStock) = withContext(Dispatchers.IO) {
        favorTickerSet.add(favor.ticker)
        favorDao.save(favor)
    }

    suspend fun pullSearchedQueries(count: Int = 30) = withContext(Dispatchers.IO) {
        val list = queryDao.getStrings(count)
        withContext(Dispatchers.Main) { searchedQueries.value = list }
    }

    suspend fun saveQuery(query: String) = withContext(Dispatchers.IO) {
        try {
            queryDao.save(UserQuery(query, System.currentTimeMillis())) // time for sorting
        } catch (e: SQLiteConstraintException) {
            Log.i("Repository", "saveQuery: not unique value $query")
        }
    }

    suspend fun pullQueryResult(query: String, limit: Int) = withContext(Dispatchers.IO){
        val list = connector.getQueryResult(query, limit)
        withContext(Dispatchers.Main) { queryResults.value = list }
    }

    fun clearQueryResults() {
        queryResults.value = ArrayList()
    }

    suspend fun pullDayChartData(ticker: String) = withContext(Dispatchers.IO) {
        val list = connector.getDayChartData(ticker)
        withContext(Dispatchers.Main) { dayChart.value = list }
    }

    suspend fun pullWeekChartData(ticker: String) = withContext(Dispatchers.IO) {
        val list = connector.getWeekChartData(ticker)
        withContext(Dispatchers.Main) { weekChart.value = list }
    }

    suspend fun pullMonthChartData(ticker: String) = withContext(Dispatchers.IO) {
        val list = connector.getMonthChartData(ticker)
        withContext(Dispatchers.Main) { monthChart.value = list }
    }

    suspend fun pullSixMonthChartData(ticker: String) = withContext(Dispatchers.IO) {
        val list = connector.getSixMonthChartData(ticker)
        withContext(Dispatchers.Main) { sixMonthChart.value = list }
    }

    suspend fun pullYearChartData(ticker: String) = withContext(Dispatchers.IO) {
        val list = connector.getYearChartData(ticker)
        withContext(Dispatchers.Main) { yearChart.value = list }
    }

    suspend fun pullAllTimeChartData(ticker: String) = withContext(Dispatchers.IO) {
        val list = connector.getAllTimeChartData(ticker)
        withContext(Dispatchers.Main) { allTimeChart.value = list }
    }

    suspend fun pullSummary(ticker: String) = withContext(Dispatchers.IO) {
        val data =  connector.getSummary(ticker)
        withContext(Dispatchers.Main) { summary.value = data }
    }

    suspend fun pullNews(ticker: String, limit: Int) = withContext(Dispatchers.IO) {
        val list = connector.getNews(ticker, limit)
        withContext(Dispatchers.Main) { news.value = list }
    }

    fun clearCardData() {
        dayChart.value = ArrayList()
        weekChart.value = ArrayList()
        monthChart.value = ArrayList()
        sixMonthChart.value = ArrayList()
        yearChart.value = ArrayList()
        allTimeChart.value = ArrayList()

        summary.value = null
        news.value = ArrayList()
    }
}

