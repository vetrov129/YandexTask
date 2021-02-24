package hi.dude.yandex.adapters

import android.app.Activity
import hi.dude.yandex.application.App
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.data.StockHolder


open class RecyclerStockAdapter(stocks: ArrayList<StockHolder>, activity: Activity) :
    AbstractRecyclerAdapter(stocks, activity) {

    override fun updateData() = Thread {
        App.updateStars()
        activity.runOnUiThread { notifyDataSetChanged() }
    }.start()

    override fun starClicked(position: Int) = Thread {
        val favor = DataFormatter.holderToFavor(stocks[position])

        if (stocks[position].isFavor) {
            App.getFavorDao().delete(favor)
            stocks[position].isFavor = false
            App.removeFavor(stocks[position].ticker)
        } else {
            App.getFavorDao().save(favor)
            stocks[position].isFavor = true
            App.favors.add(stocks[position])
        }
        activity.runOnUiThread { notifyItemChanged(position) }
    }.start()

}