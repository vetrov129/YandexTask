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

    private var isActive = true
    private val repository = Repository.getInstance()
    private var firstVisible = 0
    private var lastVisible = 0
    private var subscribed = HashSet<String>()
    private val socketData = repository.prices
    private var currentPriceData: HashMap<String, PriceData> = HashMap()
    private val delayed = HashSet<Int>() // items to be updated
    private var recyclerIsReadyUpdate = true // cannot be updated while scrolling
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

    private fun setVisibleIndexes() {
        try {
            val manager = page.recycler.layoutManager as LinearLayoutManager
            firstVisible = manager.findFirstVisibleItemPosition()
            if (firstVisible >= 5) firstVisible -= 5

            lastVisible = manager.findLastVisibleItemPosition()
            if (lastVisible + 5 < page.stocks.size) lastVisible += 5
        } catch (e: NullPointerException) {
            Log.e(TAG, "setVisibleIndexes: ", e)
        }
    }

    private suspend fun updateSubscribes() {
        while (repository.waitingForWebSocketForList) {
            delay(100)
        }
        setVisibleIndexes()
        val newSubscribed = HashSet<String>()
        // subscribe for new tickers
        for (position in firstVisible..lastVisible) {
            try {
                if (!subscribed.contains(page.stocks[position].ticker)) {
                    repository.subscribeForList(arrayOf(page.stocks[position].ticker))
                }
                newSubscribed.add(page.stocks[position].ticker)
            } catch (e: IndexOutOfBoundsException) {
                Log.e(TAG, "updateSubscribes: end of list")
                break
            }
        }
        // unsubscribe for old tickers
        repository.unsubscribeForList((subscribed - newSubscribed).toTypedArray())

        subscribed = newSubscribed
        Log.i(TAG, "updateSubscribes: subscribed ${subscribed.size} $subscribed")
    }

    private fun updateCurrentPriceData() = synchronized(socketData.value?.data!!) {
        if (socketData.value?.data == null) return
        val map = HashMap<String, PriceData>()
        socketData.value?.data?.forEach { map[it?.ticker ?: ""] = it ?: PriceData(0.0, 0, "") }
        currentPriceData = map
        socketData.value?.data = ArrayList()
    }

    private fun updateHoldersData() {
        for (position in firstVisible..lastVisible) {
            try {
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
            } catch (e: IndexOutOfBoundsException) {
                Log.e(TAG, "run: end of list")
                break
            }

        }
    }

    suspend fun run() = withContext(Dispatchers.Default) {
        while (isActive) {
            if (job.isCancelled) {
                delay(200)
                continue
            }
            updateSubscribes()
            updateCurrentPriceData()
            updateHoldersData()
            if (recyclerIsReadyUpdate) updateRecycler()
//            delay(2000)
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