package hi.dude.yandex.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.R
import hi.dude.yandex.pager.AbstractPage
import java.util.*

class StockPagerAdapter(val pageList: ArrayList<AbstractPage>) :
    RecyclerView.Adapter<StockPagerAdapter.PagerVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.page_stocks_list, parent, false)
        return PagerVH(itemView)
    }


    override fun getItemCount(): Int = pageList.size

    override fun onBindViewHolder(holder: PagerVH, position: Int) = holder.itemView.run {
        pageList[position].recycler = findViewById(R.id.rvStocksList)
        pageList[position].bind()
        pageList[position].pullData()
    }

    class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}

