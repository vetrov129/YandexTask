package hi.dude.yandex.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import hi.dude.yandex.R
import hi.dude.yandex.model.Repository
import hi.dude.yandex.model.entities.*
import kotlinx.coroutines.*
import java.io.IOException

class CardViewModel(val app: Application, val ticker: String) : AndroidViewModel(app), CoroutineScope {

    private var job = SupervisorJob()
    override var coroutineContext = Dispatchers.Main + job

    private val logHandler = CoroutineExceptionHandler { _, exception ->
        println("EXCEPTION/VIEWMODEL: \n${exception.printStackTrace()}}")
    }

    private val repository = Repository.getInstance()
    private val favorTickerSet = repository.favorTickerSet

    val dayChart: LiveData<ArrayList<ChartLine>> = repository.dayChart
    val weekChart: LiveData<ArrayList<ChartLine>> = repository.weekChart
    val monthChart: LiveData<ArrayList<ChartLine>> = repository.monthChart
    val sixMonthChart: LiveData<ArrayList<ChartLine>> = repository.sixMonthChart
    val yearChart: LiveData<ArrayList<ChartLine>> = repository.yearChart
    val allTimeChart: LiveData<ArrayList<ChartLine>> = repository.allTimeChart

    val summary: LiveData<Summary> = repository.summary
    val news: LiveData<ArrayList<NewsItem>> = repository.news

    val realTimePrice: LiveData<PriceData> = repository.realTimePrice
    private lateinit var realTimePriceJob: Job

    init {
        clearCardData()
        pullChartData()
        pullSummary()
        pullNews()
        startUpdatePriceData()
    }

    fun checkIsFavor(ticker: String): Boolean {
        return favorTickerSet.contains(ticker)
    }

    fun cancel() {
        stopUpdatePrice()
        job.cancel()
    }

    fun resume() {
        if (job.isCancelled) {
            job = SupervisorJob()
            coroutineContext = Dispatchers.Main + job
        }
    }

    fun deleteFavor(favor: StockHolder) = launch(logHandler) {
        repository.deleteFavor(favor.toFavor())
    }

    fun saveFavor(favor: StockHolder) = launch(logHandler) {
        repository.saveFavor(favor.toFavor())
    }

    fun pullNewsImages(adapter: RecyclerView.Adapter<*>) {
        if (news.value == null)
            return
        for (position in 0 until (news.value?.size ?: 0)) {
            try {
                val imageJob = launch(logHandler) {
                    withContext(Dispatchers.IO) {
                        news.value?.get(position)?.imageBitmap = Picasso.get()
                            .load(news.value?.get(position)?.imageUrl)
                            .error(R.drawable.empty)
                            .placeholder(R.drawable.empty)
                            .get()
                    }
                }
                launch(logHandler) {
                    imageJob.join()
                    adapter.notifyItemChanged(position)
                }
            } catch (e: IOException) {
                Log.e("ViewModel", "pullNewsImages:", e)
            }
        }
    }

    fun updateHolder(holder: StockHolder) {
        val quoteAsync = async { repository.getQuote(holder.ticker) }
        launch {
            val quote = quoteAsync.await()
            holder.priceClose = quote?.close
        }
    }

    private fun pullChartData() {
        launch(logHandler) { repository.pullDayChartData(ticker) }
        launch(logHandler) { repository.pullWeekChartData(ticker) }
        launch(logHandler) { repository.pullMonthChartData(ticker) }
        launch(logHandler) { repository.pullSixMonthChartData(ticker) }
        launch(logHandler) { repository.pullYearChartData(ticker) }
        launch(logHandler) { repository.pullAllTimeChartData(ticker) }
    }

    private fun clearCardData() = repository.clearCardData()

    private fun pullSummary() {
        launch(logHandler) { repository.pullSummary(ticker) }
    }

    private fun pullNews(limit: Int = 20) {
        launch(logHandler) { repository.pullNews(ticker, limit) }
    }

    private fun startUpdatePriceData() {
        realTimePriceJob = Job(job)
        val scope = CoroutineScope(Dispatchers.IO) + realTimePriceJob
        val open = launch(logHandler) {
            repository.startUpdatePriceDataOnCard(scope)
            while (repository.waitingForWebSocketForCard) {
                delay(50)
            }
        }
        launch {
            open.join()
            repository.subscribeCard(ticker)
        }
    }

    private fun stopUpdatePrice() {
        realTimePriceJob.cancel()
        clearRealTimePrice()
    }

    private fun clearRealTimePrice() {
        repository.clearRealTimePrice()
    }
}