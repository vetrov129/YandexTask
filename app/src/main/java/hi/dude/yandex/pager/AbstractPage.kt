package hi.dude.yandex.pager

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import hi.dude.yandex.data.StockHolder
import hi.dude.yandex.adapters.AbstractRecyclerAdapter

abstract class AbstractPage(var stocks: ArrayList<StockHolder>, val searchPanel: View) {

    private var readyToHide = true

    lateinit var recAdapter: AbstractRecyclerAdapter
    lateinit var recycler: RecyclerView

    fun bind() {
        setAdapter()
        recycler.adapter = recAdapter
        setListener()
    }

    private fun setListener() {
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (readyToHide) {
                    if (dy < 0) {
                        searchPanel.visibility = View.VISIBLE
                        readyToHide = false
                    }
                    if (dy > 3) {
                        searchPanel.visibility = View.GONE
                        readyToHide = false
                    }
                }
                readyToHide =
                    recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE ||
                    recyclerView.scrollState != RecyclerView.SCROLL_STATE_DRAGGING
            }
        })
    }

    abstract fun setAdapter()
    abstract fun pullData(start: Int = 0)
}