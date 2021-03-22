package hi.dude.yandex.view.pages

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.view.adapters.StocksRecyclerAdapter
import hi.dude.yandex.viewmodel.StockHolder
import hi.dude.yandex.viewmodel.ListViewModel

class Page(
    stocks: List<StockHolder>,
    val searchPanel: View?,
    resources: Resources,
    context: Context,
    private val viewModel: ListViewModel,
    starClicked: (Int) -> Unit
) {
    private var readyToHide = true

    var recAdapter: StocksRecyclerAdapter = StocksRecyclerAdapter(stocks, resources, context, viewModel, starClicked)
    lateinit var recycler: RecyclerView

    var stocks = stocks
        set(value) {
            field = value
            recAdapter.stocks = value
            Log.i("Page", "setter: size ${value.size}")
        }

    fun bind(recycler: RecyclerView) {
        this.recycler = recycler
        this.recycler.itemAnimator?.changeDuration = 0
        recycler.adapter = recAdapter
        setListener()
    }

    private fun setListener() {
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (searchPanel == null) return
                if (readyToHide) {
                    if (dy < 0) {
                        searchPanel.visibility = View.VISIBLE
                        readyToHide = false
                    }
                    if (dy > 3) {
                        searchPanel.visibility = View.GONE
                        readyToHide = false
                    }
                }
                readyToHide = recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE ||
                              recyclerView.scrollState != RecyclerView.SCROLL_STATE_DRAGGING

            }
        })

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    val manager = recycler.layoutManager as LinearLayoutManager
                    if (manager.findLastVisibleItemPosition() >=
                        recAdapter.countOfPacks * recAdapter.packSize - recAdapter.offsetToScrollLoad
                    ) {
                        viewModel.addStocks(
                            recAdapter,
                            recAdapter.countOfPacks * recAdapter.packSize,
                            (recAdapter.countOfPacks + 1) * recAdapter.packSize,
                        )
                        recAdapter.countOfPacks++
                    }
                }
            }
        })
    }
}