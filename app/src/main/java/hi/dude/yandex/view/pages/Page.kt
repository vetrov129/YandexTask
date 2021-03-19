package hi.dude.yandex.view.pages

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.view.adapters.StocksRecyclerAdapter
import hi.dude.yandex.viewmodel.StockHolder
import hi.dude.yandex.viewmodel.StockViewModel

class Page(
    stocks: List<StockHolder>,
    val searchPanel: View?,
    resources: Resources,
    context: Context,
    private val viewModel: StockViewModel,
    starClicked: (Int) -> Unit
) {
    private var readyToHide = true

    var recAdapter: StocksRecyclerAdapter = StocksRecyclerAdapter(stocks, resources, context, viewModel, starClicked)
    lateinit var recycler: RecyclerView

    var stocks = stocks
        set(value) {
            field = value
            recAdapter.stocks = value
        }

    fun bind(recycler: RecyclerView) {
        this.recycler = recycler
        recycler.adapter = recAdapter
        setListener()
    }

    private fun setListener() { // TODO: 20.03.2021 сделать чтобы поиск скрывался при свайпе вниз, а не при прокрутке
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
                readyToHide =
                    recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE ||
                            recyclerView.scrollState != RecyclerView.SCROLL_STATE_DRAGGING

                // TODO: 19.03.2021 сделать добавление элементов пачками
            }
        })
    }
}