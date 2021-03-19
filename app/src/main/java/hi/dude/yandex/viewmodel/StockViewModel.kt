package hi.dude.yandex.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.model.Repository
import hi.dude.yandex.model.entities.Quote
import hi.dude.yandex.model.entities.Stock
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
        launch {
            repository.initDao(app)
            repository.pullAllStocks()
        }
    }

    private val mutableStocks = MutableLiveData<ArrayList<StockHolder>>()
    private val mutableFavors = DataFormatter.stocksToHolders(repository.favors)

    val favors: LiveData<ArrayList<StockHolder>> = mutableFavors
    val stocks: LiveData<ArrayList<StockHolder>> = mutableStocks  // TODO: 18.03.2021 мб подтягивать в геттерах?
    val allStocks: LiveData<ArrayList<Stock>> = repository.allStocks


    fun pullFavors() = launch {
        repository.pullFavors()
        mutableFavors.value = DataFormatter.stocksToHolders(repository.favors).value
    }

    fun pullAllStocks() = launch {
        if (stocks.value?.size == 0 || stocks.value == null) {
            repository.pullAllStocks()
        }
    }

    fun updateStar(adapter: RecyclerView.Adapter<*>) = launch {
        try {
            for (i in stocks.value!!.indices) {
                try {
                    stocks.value!![i].isFavor = checkIsFavor(stocks.value!![i].ticker)
                    adapter.notifyItemChanged(i)
                } catch (e: NullPointerException) {
                    Log.e("ViewModel", "updateStar: NPE inside, continue cycle")
                }
            }
        } catch (e: NullPointerException) {
            Log.e("ViewModel", "updateStar: NPE outside, break cycle")
        }
    }

    private fun checkIsFavor(ticker: String): Boolean {
        return favors.value?.any { it.ticker == ticker } ?: false
    }

    fun addStocks(adapter: RecyclerView.Adapter<*>, start: Int = 0, until: Int = 30) {
        try {
            val newList = ArrayList<StockHolder>()
            if (mutableStocks.value != null)
                newList.addAll(mutableStocks.value!!)
            for (i in start until until) {
                newList.add(StockHolder(allStocks.value!![i]))
            }
            mutableStocks.value = newList
            adapter.notifyDataSetChanged()
            Log.i("ViewModel", "addStocks: stocks added")
        } catch (e: IndexOutOfBoundsException) {
            Log.e("ViewModel", "addStocks: end of list, added ${stocks.value?.size}")
        } catch (e: NullPointerException) {
            Log.e("ViewModel", "addStocks: ", e)
        }
    }

    fun pullHolderData(start: Int, end: Int, adapter: RecyclerView.Adapter<*>, list: List<StockHolder>) {
        for (position in start until end) {
            launch(handlerLong) {
                try {
                    list[position].pullData(list[position].ticker)
                    adapter.notifyItemChanged(position)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullData: end of list")
                }
            }
        }
    }

    suspend fun getQuote(ticker: String): Quote? {
        return repository.getQuote(ticker)
    }
}