package hi.dude.yandex.view.adapters

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.R
import hi.dude.yandex.view.activities.StockCardActivity
import hi.dude.yandex.viewmodel.StockHolder
import hi.dude.yandex.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.list_item_stock.view.*

class StocksRecyclerAdapter(
    stocks: List<StockHolder>,
    private val resources: Resources,
    private val context: Context,
    private val viewModel: ListViewModel,
    var starClicked: (Int) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var stocks: List<StockHolder> = stocks
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var countOfPacks = 1
    val packSize = 20
    val offsetToScrollLoad = 10

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
            view.tvDiffStock.setTextColor(resources.getColor(R.color.colorRed))
        else
            view.tvDiffStock.setTextColor(resources.getColor(R.color.colorGreen))

        if (viewModel.checkIsFavor(stock.ticker)) view.ivStarStock.setImageResource(R.drawable.ic_yellow_star)
        else view.ivStarStock.setImageResource(R.drawable.ic_gray_star)

        if (position % 2 == 0) view.containerStock.setBackgroundResource(R.color.colorBG)
        else view.containerStock.setBackgroundResource(R.color.colorPrimary)

        view.ivStarStock.setOnClickListener { starClicked(position) }
        view.setOnClickListener { itemClicked(position) }
    }

    private fun itemClicked(position: Int) {
        val intent = Intent(context, StockCardActivity::class.java)
        intent.putExtra("ticker", stocks[position].ticker)
        intent.putExtra("company", stocks[position].company)
        intent.putExtra("price", stocks[position].price)
        intent.putExtra("change", stocks[position].change)
        intent.putExtra("currency", stocks[position].currency)
        context.startActivity(intent)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}