package hi.dude.yandex.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.application.App
import hi.dude.yandex.R
import kotlinx.android.synthetic.main.activity_stocks_list.*
import kotlinx.android.synthetic.main.activity_stocks_list.view.*
import kotlinx.android.synthetic.main.list_item_bubble.view.*

class RecyclerBubblesAdapter(private var bubbles: List<String>, private val activity: Activity) :
    RecyclerView.Adapter<RecyclerBubblesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_bubble, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tvBubble.text = bubbles[position]
        holder.itemView.setOnClickListener { bubbleClicked(position) }
    }

    private fun bubbleClicked(position: Int) {
        activity.searchContainer.edSearch.setText(bubbles[position])
    }

    override fun getItemCount(): Int = bubbles.size

    fun setPopularBubbles() {
        bubbles = App.getPopularCompany()
    }

    fun setSearchedBubbles() { // update the list of requests
        bubbles = App.getSearchedQueries()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}