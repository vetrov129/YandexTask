package hi.dude.yandex.adapters

import android.app.Activity
import hi.dude.yandex.application.App
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.data.StockHolder

class RecyclerFavorAdapter(stocks: ArrayList<StockHolder>, activity: Activity) :
    AbstractRecyclerAdapter(stocks, activity) {

    override fun updateData() = Thread {
        stocks = App.updateFavor {}
        activity.runOnUiThread { notifyDataSetChanged() }
        pullData()
    }.start()

    override fun starClicked(position: Int) = Thread {
        val favor = DataFormatter.holderToFavor(stocks[position])

        App.getFavorDao().delete(favor)
        stocks.remove(stocks[position])
        activity.runOnUiThread { notifyDataSetChanged() }
    }.start()

}
