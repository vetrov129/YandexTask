package hi.dude.yandex.pager

import android.app.Activity
import android.view.View
import hi.dude.yandex.data.StockHolder
import hi.dude.yandex.adapters.RecyclerFavorAdapter

class FavorPage(stocks: ArrayList<StockHolder>, searchPanel: View, private val activity: Activity)
    : AbstractPage(stocks, searchPanel) {

    override fun setAdapter() {
        recAdapter = RecyclerFavorAdapter(stocks, activity)
    }

    override fun pullData(start: Int) = Thread {
            recAdapter.pullData()
        }.start()

}