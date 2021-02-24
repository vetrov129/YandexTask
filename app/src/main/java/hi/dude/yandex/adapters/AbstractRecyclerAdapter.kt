package hi.dude.yandex.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.R
import hi.dude.yandex.activities.StockCardActivity
import hi.dude.yandex.data.StockHolder
import kotlinx.android.synthetic.main.list_item_stock.view.*

abstract class AbstractRecyclerAdapter(open var stocks: ArrayList<StockHolder>, protected val activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_stock, parent, false)
        )


    override fun getItemCount(): Int = stocks.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val stock = stocks[position]
        val view = holder.itemView

        view.ivImage.setImageBitmap(stock.image)
        view.tvTickerStock.text = stock.ticker
        view.tvCompanyStock.text = stock.company
        view.tvPriceStock.text = stock.price
        view.tvDiffStock.text = stock.change

        if (stock.change != "" && stock.change[0] == '-')
            view.tvDiffStock.setTextColor(activity.resources.getColor(R.color.colorRed))
        else
            view.tvDiffStock.setTextColor(activity.resources.getColor(R.color.colorGreen))

        if (stock.isFavor) view.ivStarStock.setImageResource(R.drawable.ic_yellow_star)
        else view.ivStarStock.setImageResource(R.drawable.ic_gray_star)

        if (position % 2 == 0) view.containerStock.setBackgroundResource(R.color.colorBG)
        else view.containerStock.setBackgroundResource(R.color.colorPrimary)

        view.ivStarStock.setOnClickListener { starClicked(position) }
        view.setOnClickListener { itemClicked(position) }
    }

    fun pullData(start: Int = 0) { // update price, change and image
        for (i in start until stocks.size) {
            try {
                stocks[i].pullData()
                activity.runOnUiThread { notifyItemChanged(i) }
            } catch (e: Exception) {
                Log.e("RecyclerAdapter", "pullData: ", e)
            }
        }
    }

    private fun itemClicked(position: Int) {
        val intent = Intent(activity, StockCardActivity::class.java)
        intent.putExtra("ticker", stocks[position].ticker)
        intent.putExtra("company", stocks[position].company)
        intent.putExtra("price", stocks[position].price)
        intent.putExtra("change", stocks[position].change)
        intent.putExtra("isFavor", stocks[position].isFavor)
        intent.putExtra("currency", stocks[position].currency)
        intent.putExtra("image", stocks[position].image)
        activity.startActivity(intent)
    }

    abstract fun starClicked(position: Int)

    abstract fun updateData() // find changes made in other components

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}