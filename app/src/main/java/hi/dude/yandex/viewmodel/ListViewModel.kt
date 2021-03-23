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
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList

class ListViewModel(val app: Application) : AndroidViewModel(app), CoroutineScope {

    private var job = SupervisorJob()
    override var coroutineContext = Dispatchers.Main + job

    private val logHandler = CoroutineExceptionHandler { _, exception ->
        println("EXCEPTION/VIEWMODEL: \n${exception.printStackTrace()}}")
    }

    private val repository = Repository.getInstance()

    init {
        val repositoryJob = launch(logHandler) {
            repository.init(app)
        }
        launch {
            repositoryJob.join()
            pullFavors()
            pullSearchedQueries()
        }
        launch(logHandler) {
            repository.pullAllStocks()
        }
    }

    private val mutableStocks = MutableLiveData<ArrayList<StockHolder>>()
    private val favorTickerSet = repository.favorTickerSet

    val favors: LiveData<SortedMap<String, FavorStock>> = repository.favors
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
            val image = launch(logHandler) {
                try {
                    list[position].pullImage(list[position].ticker)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullHolderData: end of list")
                }
            }
            val price = launch(logHandler) {
                image.join()
                adapter.notifyItemChanged(position)
                try {
                    list[position].pullChangeAndPrice(list[position].ticker)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullHolderData: end of list")
                }
            }
            launch(logHandler) {
                price.join()
                adapter.notifyItemChanged(position)
            }
        }
    }

    fun pullFavors() {
        val favorJob = launch(logHandler) { repository.pullFavors() }
        launch(logHandler) {
            favorJob.join()
            repository.fillFavorSet()
        }
    }

    fun checkIsFavor(ticker: String): Boolean {
        return favorTickerSet.contains(ticker)
    }

    fun saveFavor(favor: StockHolder, adapter: RecyclerView.Adapter<*>, position: Int) {
        val favorJob = saveFavor(favor)
        launch(logHandler) {
            favorJob.join()
            adapter.notifyItemChanged(position)
        }
    }

    fun deleteFavor(favor: StockHolder, adapter: RecyclerView.Adapter<*>, position: Int) {
        val favorJob = deleteFavor(favor)
        launch(logHandler) {
            favorJob.join()
            adapter.notifyItemChanged(position)
        }
    }

    fun saveFavor(favor: StockHolder) = launch(logHandler) {
        repository.saveFavor(favor.toFavor())
//        pullFavors()
    }

    fun deleteFavor(favor: StockHolder) = launch(logHandler) {
        repository.deleteFavor(favor.toFavor())
//        pullFavors()
    }

    fun setFavorHolders(page: Page) {
        Log.i("ViewModel", "setFavorHolders: ")
        val holders = ArrayList<StockHolder>()
        favors.value?.forEach { holders.add(StockHolder(it.value)) }
        page.stocks = holders
        pullHolderData(0, holders.size, page.recAdapter, holders)
        page.recAdapter.notifyDataSetChanged()
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

    fun pullSearchedQueries(count: Int = 30) = launch(logHandler) {
        repository.pullSearchedQueries(count)
    }

    fun saveQuery(query: String) = launch(logHandler) {
        if (query != "") {
            repository.saveQuery(query)
            repository.pullSearchedQueries()
        } // TODO: 20.03.2021 возможно нужно ждать окончания первого метода
    }

    fun runSearch(query: String, limit: Int = 4) {
        searchJob.cancel()
        searchJob = launch(logHandler) {
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