package hi.dude.yandex.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import hi.dude.yandex.R
import hi.dude.yandex.model.Repository
import hi.dude.yandex.model.entities.*
import kotlinx.coroutines.*
import java.io.IOException

class CardViewModel(val app: Application) : AndroidViewModel(app), CoroutineScope {

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

    fun pullChartData(ticker: String) {
        launch(logHandler) { repository.pullDayChartData(ticker) }
        launch(logHandler) { repository.pullWeekChartData(ticker) }
        launch(logHandler) { repository.pullMonthChartData(ticker) }
        launch(logHandler) { repository.pullSixMonthChartData(ticker) }
        launch(logHandler) { repository.pullYearChartData(ticker) }
        launch(logHandler) { repository.pullAllTimeChartData(ticker) }
    }

    fun clearCardData() = repository.clearCardData()

    fun pullSummary(ticker: String) {
        launch(logHandler) { repository.pullSummary(ticker) }
    }

    fun pullNews(ticker: String, limit: Int = 20) {
        launch(logHandler) { repository.pullNews(ticker, limit) }
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

    fun startUpdatePriceData(ticker: String) {
        realTimePriceJob = Job(job)
        val scope = CoroutineScope(Dispatchers.IO) + realTimePriceJob
        launch(logHandler) {
            repository.startUpdatePriceData(ticker, scope)
        }
    }

    fun stopUpdatePrice() {
        clearRealTimePrice()
        val closeJob = launch(logHandler) {
            repository.stopUpdatePrice()
        }
        launch {
            closeJob.join()
            realTimePriceJob.cancel()
        }
    }

    private fun clearRealTimePrice() {
        repository.clearRealTimePrice()
    }
}