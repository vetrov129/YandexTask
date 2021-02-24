package hi.dude.yandex.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import hi.dude.yandex.application.App
import hi.dude.yandex.R
import hi.dude.yandex.adapters.RecyclerNewsAdapter
import hi.dude.yandex.data.api.models.NewsItem
import kotlinx.android.synthetic.main.fragment_news.*

class NewsFragment(val ticker: String): Fragment() {

    private var news = ArrayList<NewsItem>()
    private lateinit var adapter: RecyclerNewsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvNews.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        adapter = RecyclerNewsAdapter(news, context as Activity)
        rvNews.adapter = adapter
        Thread {
            news = App.connector.getNews(ticker)
            (context as Activity).runOnUiThread { adapter.news = news }
        }.start()

    }

}