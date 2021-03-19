package hi.dude.yandex.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.R
import kotlinx.android.synthetic.main.list_item_bubble.view.*

class BubblesRecyclerAdapter(bubbles: List<String>, val bubbleClicked: (String) -> Unit) :
    RecyclerView.Adapter<BubblesRecyclerAdapter.ViewHolder>() {

    var bubbles: List<String> = bubbles
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_bubble, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tvBubble.text = bubbles[position]
        holder.itemView.setOnClickListener { bubbleClicked(bubbles[position]) }
    }

    override fun getItemCount(): Int = bubbles.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}