package hi.dude.yandex.adapters

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.R
import hi.dude.yandex.data.api.models.NewsItem
import kotlinx.android.synthetic.main.list_item_news.view.*

class RecyclerNewsAdapter(news: ArrayList<NewsItem>, val activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var news = news
        set(value) {
            field = value
            notifyDataSetChanged()
            Thread { pullImages() }.start()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_news, parent, false)
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val view = holder.itemView
        val item = news[position]

        view.ivImageNews.setImageBitmap(item.imageBitmap)
        view.titleNews.text = item.title
        view.textNews.text = item.text
        view.dateNews.text = DataFormatter.toPrettyDate(item.date)

        view.setOnClickListener { itemClicked(holder, position) }
        view.toWebsite.setOnClickListener { toWebsiteClicked(position) }
        view.arrowToWebsite.setOnClickListener { toWebsiteClicked(position) }

        if (item.fullSize)
            setVisibility(View.VISIBLE, holder)
        else
            setVisibility(View.GONE, holder)

    }

    private fun itemClicked(holder: RecyclerView.ViewHolder, position: Int) {
        if (news[position].fullSize) {
            setVisibility(View.GONE, holder)
            news[position].fullSize = false
        } else {
            setVisibility(View.VISIBLE, holder)
            news[position].fullSize = true
        }
    }

    private fun setVisibility(visibility: Int, holder: RecyclerView.ViewHolder) {
        holder.itemView.textNews.visibility = visibility
        holder.itemView.dateNews.visibility = visibility
        holder.itemView.toWebsite.visibility = visibility
        holder.itemView.arrowToWebsite.visibility = visibility
    }

    private fun toWebsiteClicked(position: Int) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(news[position].url)
        activity.startActivity(intent)
    }

    override fun getItemCount(): Int = news.size

    private fun pullImages() {
        for (i in news.indices) {
            try {
                news[i].pullImage()
                activity.runOnUiThread { notifyItemChanged(i) }
            } catch (e: Exception) {
                Log.e("RecyclerNewsAdapter", "pullData: ", e)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}