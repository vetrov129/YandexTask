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
import hi.dude.yandex.view.pages.Page
import kotlinx.coroutines.*

class ListViewModel(val app: Application) : AndroidViewModel(app), CoroutineScope {

    private var job = SupervisorJob()
    override var coroutineContext = Dispatchers.Main + job

    private val handler = CoroutineExceptionHandler { _, exception ->
        println("EXCEPTION/VIEWMODEL: \n${exception.printStackTrace()}}")
    }

    private val repository = Repository.getInstance()

    init {
        launch(handler) {
            repository.init(app)
            repository.pullAllStocks()
            repository.pullFavors()
            repository.pullSearchedQueries()
        }
    }

    private val mutableStocks = MutableLiveData<ArrayList<StockHolder>>()
    private val favorTickerSet = repository.favorTickerSet

    val favors: LiveData<List<FavorStock>> = repository.favors
    val stocks: LiveData<ArrayList<StockHolder>> = mutableStocks
    val allStocks: LiveData<ArrayList<Stock>> = repository.allStocks

    val searchedQueries: LiveData<List<String>> = repository.searchedQueries
    val queryResults: LiveData<ArrayList<QueryResult>> = repository.queryResults

    private var searchJob: Job = Job()

    fun cancel() {
        job.cancel()
    }

    fun resume() {
        if (job.isCancelled) {
            job = SupervisorJob()
            coroutineContext = Dispatchers.Main + job
        }
    }

    fun pullFavors() {
        val favorJob = launch(handler) { repository.pullFavors() }
        launch(handler) {
            favorJob.join()
            repository.fillFavorSet()
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
            val image = launch(handler) {
                try {
                    list[position].pullImage(list[position].ticker)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullHolderData: end of list")
                }
            }
            val price = launch(handler) {
                image.join()
                adapter.notifyItemChanged(position)
                try {
                    list[position].pullChangeAndPrice(list[position].ticker)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullHolderData: end of list")
                }
            }
            launch(handler) {
                price.join()
                adapter.notifyItemChanged(position)
            }
        }
    }

    fun deleteFavor(favor: StockHolder) = launch(handler) {
        repository.deleteFavor(favor.toFavor())
        pullFavors()
    }

    fun deleteFavor(favor: StockHolder, adapter: RecyclerView.Adapter<*>, position: Int) {
        val favorJob = deleteFavor(favor)
        launch(handler) {
            favorJob.join()
            adapter.notifyItemChanged(position)
        }
    }

    fun saveFavor(favor: StockHolder) = launch(handler) {
        repository.saveFavor(favor.toFavor())
        pullFavors()
    }

    fun saveFavor(favor: StockHolder, adapter: RecyclerView.Adapter<*>, position: Int) {
        val favorJob = saveFavor(favor)
        launch(handler) {
            favorJob.join()
            adapter.notifyItemChanged(position)
        }
    }

    fun setFavorHolders(page: Page) {
        val holders = ArrayList<StockHolder>()
        favors.value?.forEach { holders.add(StockHolder(it)) }
        page.stocks = holders
        pullHolderData(0, holders.size, page.recAdapter, holders)
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

    fun pullSearchedQueries(count: Int = 30) = launch(handler) {
        repository.pullSearchedQueries(count)
    }

    fun saveQuery(query: String) = launch(handler) {
        if (query != "") {
            repository.saveQuery(query)
            repository.pullSearchedQueries()
        } // TODO: 20.03.2021 возможно нужно ждать окончания первого метода
    }

    fun runSearch(query: String, limit: Int = 4) {
        searchJob.cancel()
        searchJob = launch {
            repository.pullQueryResult(query, limit)
        }
    }

    fun getResultHolders(): List<StockHolder> {
        if (queryResults.value == null) {
            Log.i("ViewModel", "getResultHolders: queryResults.value == null")
            return ArrayList()
        }
        val holders = ArrayList<StockHolder>()
        queryResults.value?.forEach { holders.add(StockHolder(it)) }
        Log.i("ViewModel", "getResultHolders: queryResults.size ${queryResults.value?.size}")
        return holders
    }

    fun clearQueryResults() {
        repository.clearQueryResults()
    }
}