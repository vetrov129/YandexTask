package hi.dude.yandex.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.R
import hi.dude.yandex.view.pages.Page
import java.util.*

class StocksPagerAdapter(val pageList: ArrayList<Page>) :
    RecyclerView.Adapter<StocksPagerAdapter.PagerVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.page_stocks_list, parent, false)
        return PagerVH(itemView)
    }

    override fun getItemCount(): Int = pageList.size

    override fun onBindViewHolder(holder: PagerVH, position: Int) = holder.itemView.run {
        pageList[position].bind(findViewById(R.id.rvStocksList))
    }

    class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}

