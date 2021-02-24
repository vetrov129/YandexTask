package hi.dude.yandex.adapters

import android.app.Activity
import android.util.Log
import hi.dude.yandex.data.StockHolder

class RecyclerResultsAdapter(stocks: ArrayList<StockHolder>, activity: Activity) :
    RecyclerStockAdapter(stocks, activity) {

    override var stocks: ArrayList<StockHolder> = stocks
        set(value) {
            field = value
            Log.i("ResultAdapter", "set stocks ${value.size}")
            activity.runOnUiThread { notifyDataSetChanged() }
            pullData()
        }
}