package hi.dude.yandex.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.model.Repository
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

    private val repository = Repository(app)

    private val mutableStocks = MutableLiveData<ArrayList<StockHolder>>()
    private val mutableFavors = DataFormatter.stocksToHolders(repository.favors)

    val favors: LiveData<ArrayList<StockHolder>> = mutableFavors
    val stocks: LiveData<ArrayList<StockHolder>> = mutableStocks  // TODO: 18.03.2021 мб подтягивать в геттерах?


    init {
        launch {
            repository.initDao()
        }
    }

    fun pullFavors() = launch {
        repository.pullFavors()
        mutableFavors.value = DataFormatter.stocksToHolders(repository.favors).value
    }

    fun pullStocks() = launch {
        if (stocks.value?.size == 0) {
            repository.pullStocks()
            addStocks()
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

    fun addStocks(start: Int = 0, until: Int = 30) {
        try {
            for (i in start until until) {
                stocks.value?.add(StockHolder(repository.allStocks[i]))
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e("ViewModel", "addStocks: end of list, added ${stocks.value?.size}")
        }
    }

    fun pullHolderData(start: Int, end: Int, adapter: RecyclerView.Adapter<*>, list: List<StockHolder>) {
        for (position in start until end) {
            launch(handlerLong) {
                try {
                    list[position].pullData()
                    adapter.notifyItemChanged(position)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("ViewModel", "pullData: end of list")
                }

            }
        }
    }
}