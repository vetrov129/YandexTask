package hi.dude.yandex.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import hi.dude.yandex.R
import hi.dude.yandex.view.adapters.NewsRecyclerAdapter
import hi.dude.yandex.viewmodel.CardViewModel
import kotlinx.android.synthetic.main.fragment_news.*

class NewsFragment(val ticker: String, val viewModel: CardViewModel): Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvNews.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        val adapter = NewsRecyclerAdapter(viewModel.news.value ?: ArrayList(), context!!, viewModel)
        rvNews.adapter = adapter

        viewModel.news.observe(this) {
            adapter.news = it ?: ArrayList()
        }
    }

}