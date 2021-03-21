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
import hi.dude.yandex.model.entities.ChartLine
import hi.dude.yandex.model.entities.NewsItem
import hi.dude.yandex.model.entities.Summary
import kotlinx.coroutines.*
import java.io.IOException

class CardViewModel(val app: Application) : AndroidViewModel(app), CoroutineScope {

    private var job = SupervisorJob()
    override var coroutineContext = Dispatchers.Main + job

    private val handler = CoroutineExceptionHandler { _, exception ->
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

    fun checkIsFavor(ticker: String): Boolean {
        return favorTickerSet.contains(ticker)
    }

    fun cancel() {
        job.cancel()
    }

    fun resume() {
        if (job.isCancelled) {
            job = SupervisorJob()
            coroutineContext = Dispatchers.Main + job
        }
    }

    fun deleteFavor(favor: StockHolder) = launch(handler) {
        repository.deleteFavor(favor.toFavor())
    }

    fun saveFavor(favor: StockHolder) = launch(handler) {
        repository.saveFavor(favor.toFavor())
    }

    fun pullChartData(ticker: String) {
        launch(handler) { repository.pullDayChartData(ticker) }
        launch(handler) { repository.pullWeekChartData(ticker) }
        launch(handler) { repository.pullMonthChartData(ticker) }
        launch(handler) { repository.pullSixMonthChartData(ticker) }
        launch(handler) { repository.pullYearChartData(ticker) }
        launch(handler) { repository.pullAllTimeChartData(ticker) }
    }

    fun clearCardData() = repository.clearCardData()

    fun pullSummary(ticker: String) {
        launch(handler) { repository.pullSummary(ticker) }
    }

    fun pullNews(ticker: String, limit: Int = 20) {
        launch(handler) { repository.pullNews(ticker, limit) }
    }

    fun pullNewsImages(adapter: RecyclerView.Adapter<*>) {
        if (news.value == null)
            return
        for (position in 0 until (news.value?.size ?: 0)) {
            try {
                val imageJob = launch(handler) {
                    withContext(Dispatchers.IO) {
                        news.value?.get(position)?.imageBitmap = Picasso.get()
                            .load(news.value?.get(position)?.imageUrl)
                            .error(R.drawable.empty)
                            .placeholder(R.drawable.empty)
                            .get()
                    }
                }
                launch(handler) {
                    imageJob.join()
                    adapter.notifyItemChanged(position)
                }
            } catch (e: IOException) {
                Log.e("ViewModel", "pullNewsImages:", e)
            }
        }
    }
}