package hi.dude.yandex.viewmodel

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.model.Repository
import hi.dude.yandex.model.entities.PriceData
import hi.dude.yandex.view.pages.Page
import kotlinx.coroutines.*

class PriceUpdater(private val page: Page, var job: Job) {

    val TAG = "PriceUpdater"

    private val repository = Repository.getInstance()
    private var currentVisibleElements: HashMap<String, StockHolder> = HashMap()
    private var previewsVisibleElements: HashMap<String, StockHolder> = HashMap()
    private var firstVisible = 0
    private var lastVisible = 0
    private var subscribed = HashSet<String>()
    private val socketData = repository.prices

    private var currentPriceData: HashMap<String, PriceData> = HashMap()

    private var isActive = true

    private val delayed = HashSet<Int>()
    private var recyclerIsReadyUpdate = true
        set(value) {
            field = value
            if (value) CoroutineScope(Dispatchers.Main).launch { updateRecycler() }
        }

    init {
        page.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerIsReadyUpdate = newState == RecyclerView.SCROLL_STATE_IDLE
            }
        })
    }

    private fun getVisibleElements(): HashMap<String, StockHolder> {
        val manager = page.recycler.layoutManager as LinearLayoutManager
        firstVisible = manager.findFirstVisibleItemPosition()
        if (firstVisible >= 5) firstVisible -= 5

        lastVisible = manager.findLastVisibleItemPosition()
        if (lastVisible + 5 < page.stocks.size) lastVisible += 5

        return page.stocks.subList(firstVisible, lastVisible).map { it.ticker to it }.toMap(HashMap())
    }

    private fun subscribeNew() {
        val newTickers = (currentVisibleElements - previewsVisibleElements.keys).keys.toTypedArray()
        repository.subscribeForList(newTickers)
        subscribed.addAll(newTickers)
    }

    private fun unsubscribeOld() {
        val oldTickers = (previewsVisibleElements - currentVisibleElements.keys).keys.toTypedArray()
        repository.unsubscribeForList(oldTickers)
        subscribed.removeAll(oldTickers)
    }

    private suspend fun updateSubscribes() {
        while (repository.waitingForWebSocketForList) {
            delay(100)
        }
        subscribeNew()
        unsubscribeOld()
        Log.i(TAG, "updateSubscribes: count of subscribed ${subscribed.size} $subscribed")
    }

    private fun setCurrent(data: List<PriceData?>?) {
        if (data == null) return
        val map = HashMap<String, PriceData>()
        data.forEach { map[it?.ticker ?: ""] = it ?: PriceData(0.0, 0, "") }
        currentPriceData = map
    }

    private fun updateCurrentPriceData() {
        setCurrent(socketData.value?.data)
    }

    suspend fun run() = withContext(Dispatchers.Default) {
        while (isActive) {
            if (job.isCancelled) {
                delay(200)
                continue
            }
            currentVisibleElements = getVisibleElements()
            updateSubscribes()
            updateCurrentPriceData()
            for (position in firstVisible..lastVisible) {
                if (currentPriceData.containsKey(page.stocks[position].ticker)) {
                    page.stocks[position].priceDouble = currentPriceData[page.stocks[position].ticker]?.price
                    page.stocks[position].price = DataFormatter.addCurrency(
                        page.stocks[position].priceDouble,
                        page.stocks[position].currency,
                        true
                    )
                    page.stocks[position].change = DataFormatter.getChange(
                        page.stocks[position].priceDouble,
                        page.stocks[position].priceClose,
                        page.stocks[position].currency
                    )
                    delayed.add(position)
                }
            }
            previewsVisibleElements = currentVisibleElements
            if (recyclerIsReadyUpdate) updateRecycler()
            delay(2000)
        }
    }

    private suspend fun updateRecycler() = withContext(Dispatchers.Main) {
        delayed.forEach{ page.recAdapter.notifyItemChanged(it) }
        delayed.clear()
    }

    fun stop() {
        isActive = false
    }
}