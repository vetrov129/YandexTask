package hi.dude.yandex.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.model.Repository
import hi.dude.yandex.model.entities.Stock
import hi.dude.yandex.model.entities.FavorStock
import hi.dude.yandex.model.entities.QueryResult
import kotlinx.coroutines.*

class StockViewModel(val app: Application) : AndroidViewModel(app), CoroutineScope {

    private var job = SupervisorJob()
    override var coroutineContext = Dispatchers.Main + job

    private val handlerLong = CoroutineExceptionHandler { _, exception ->
        println("EXCEPTION/VIEWMODEL: Caught ${exception.printStackTrace()}}")
    }

    private val handlerShort = CoroutineExceptionHandler { _, exception ->
        println("EXCEPTION/VIEWMODEL: Caught $exception}")
    }

    private val repository = Repository.getInstance()

    init {
        launch(handlerLong) {
            repository.init(app)
            repository.pullAllStocks()
            repository.pullFavors()
            repository.pullSearchedQueries()
        }
    }

    private val mutableStocks = MutableLiveData<ArrayList<StockHolder>>()
    private val favorTickerSet = HashSet<String>()

    val favors: LiveData<List<FavorStock>> = repository.favors
    val stocks: LiveData<ArrayList<StockHolder>> = mutableStocks
    val allStocks: LiveData<ArrayList<Stock>> = repository.allStocks

    // TODO: 18.03.2021 мб подтягивать в геттерах?
    val searchedQueries: LiveData<List<String>> = repository.searchedQueries
    val queryResult: LiveData<ArrayList<QueryResult>> = repository.queryResult

    fun pullFavors() {
        val favorJob = launch(handlerLong) { repository.pullFavors() }
        launch(handlerLong) {
            favorJob.join()
            favors.value?.forEach { favorTickerSet.add(it.ticker) }
        }
    }

    fun checkIsFavor(ticker: String): Boolean {
        return favorTickerSet.contains(ticker)
    }

    fun addStocks(adapter: RecyclerView.Adapter<*>, start: Int = 0, until: Int = 20) {
        try {
            if (mutableStocks.value == null) {
                mutableStocks.value = ArrayList()
            }
            for (i in start until until) {
                mutableStocks.value?.add(StockHolder(allStocks.value!![i]))
            }
            adapter.notifyItemRangeInserted(start, until - start)
            pullHolderData(start, until, adapter, mutableStocks.value)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("ViewModel", "addStocks: end of list, added ${stocks.value?.size}")
        } catch (e: NullPointerException) {
            Log.e("ViewModel", "addStocks: ", e)
        }
    }

    fun pullHolderData(start: Int, until: Int, adapter: RecyclerView.Adapter<*>, list: List<StockHolder>?) {
        if (list == null)
            return
        for (position in start until until) {
            val image = launch(handlerLong) {
                try {
                    list[position].pullImage(list[position].ticker)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullHolderData: end of list")
                }
            }
            val price = launch(handlerLong) {
                image.join()
                adapter.notifyItemChanged(position)
                try {
                    list[position].pullChangeAndPrice(list[position].ticker)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullHolderData: end of list")
                }
            }
            launch(handlerLong) {
                price.join()
                adapter.notifyItemChanged(position)
            }
        }
    }

    fun deleteFavor(favor: StockHolder) = launch(handlerLong) {
        favorTickerSet.remove(favor.ticker)
        repository.deleteFavor(favor.toFavor())
        pullFavors()
    }

    fun deleteFavor(favor: StockHolder, adapter: RecyclerView.Adapter<*>, position: Int) {
        val favorJob = deleteFavor(favor)
        launch(handlerLong) {
            favorJob.join()
            adapter.notifyItemChanged(position)
        }
    }

    fun saveFavor(favor: StockHolder) = launch(handlerLong) {
        favorTickerSet.add(favor.ticker)
        repository.saveFavor(favor.toFavor())
        pullFavors()
    }

    fun saveFavor(favor: StockHolder, adapter: RecyclerView.Adapter<*>, position: Int) {
        val favorJob = saveFavor(favor)
        launch(handlerLong) {
            favorJob.join()
            adapter.notifyItemChanged(position)
        }
    }

    fun getFavorHolders(): ArrayList<StockHolder> {
        val holders = ArrayList<StockHolder>()
        favors.value?.forEach { holders.add(StockHolder(it)) }
        return holders
    }

    fun getPopularCompany(count: Int = 20): ArrayList<String> {
        val companies = ArrayList<String>()
        for (i in 0 until count) {
            try {
                companies.add(DataFormatter.companyToQuery(stocks.value!![i].company))
            } catch (e: IndexOutOfBoundsException) {
                // empty
            }
        }
        return companies
    }

    fun pullSearchedQueries(count: Int = 30) = launch(handlerLong) {
        repository.pullSearchedQueries(count)
    }

    fun saveQuery(query: String) = launch(handlerLong) {
        if (query != "") {
            repository.saveQuery(query)
            repository.pullSearchedQueries()
        } // TODO: 20.03.2021 возможно нужно ждать окончания первого метода
    }

    fun runSearch(query: String, limit: Int = 4) = launch {
        repository.pullQueryResult(query, limit)
    }
}