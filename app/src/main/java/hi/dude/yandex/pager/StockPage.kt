package hi.dude.yandex.pager

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.application.App
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.data.StockHolder
import hi.dude.yandex.adapters.RecyclerStockAdapter

class StockPage(stocks: ArrayList<StockHolder>, searchPanel: View, private val activity: Activity)
    : AbstractPage(stocks, searchPanel) {

    private var countOfPacks = 1
    private val packSize = 20

    override fun setAdapter() {
        recAdapter = RecyclerStockAdapter(stocks, activity)
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {// add stocks if scrolled to almost last
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    val manager = recycler.layoutManager as LinearLayoutManager
                    if (manager.findLastVisibleItemPosition() >= countOfPacks * packSize - 5) {
                        stocks.addAll(DataFormatter.getStockHolders(App.stocks, countOfPacks * packSize, packSize))
                        recycler.post { recAdapter.notifyDataSetChanged() }
                        pullData(countOfPacks * packSize)
                        countOfPacks++
                        App.holders = stocks
                    }
                }
            }
        })
    }

    override fun pullData(start: Int)  = Thread {
            recAdapter.pullData(start)
        }.start()
}