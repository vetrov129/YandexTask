package hi.dude.yandex.activities

import android.animation.AnimatorInflater
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import hi.dude.yandex.application.App
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.R
import hi.dude.yandex.adapters.RecyclerResultsAdapter
import hi.dude.yandex.adapters.RecyclerBubblesAdapter
import hi.dude.yandex.pager.FavorPage
import hi.dude.yandex.pager.StockPage
import hi.dude.yandex.adapters.StockPagerAdapter
import hi.dude.yandex.data.room.UserQuery
import kotlinx.android.synthetic.main.activity_stocks_list.*


class StocksListActivity : AppCompatActivity() {

    private lateinit var vpAdapter: StockPagerAdapter

    private var isShowMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stocks_list)

        supportActionBar?.hide()

        setUpPager()
        setUpSearchPanel()
        setUpHints()
        setUpResultsPanel()
    }

    override fun onRestart() {
        super.onRestart()
        try {
            vpAdapter.pageList[viewPager2.currentItem].recAdapter.updateData() // pull new favor
        } catch (e: UninitializedPropertyAccessException) {
            Log.e("StockListActivity", "onRestart: UninitializedPropertyAccessException ")
        }
    }

    private fun setUpSearchPanel() {
        ibClearSearch.visibility = View.GONE
        ibBackSearch.visibility = View.GONE

        ibClearSearch.setOnClickListener { searchClearClicked() }
        ibBackSearch.setOnClickListener { searchBackClicked() }

        edSearch.setOnFocusChangeListener { _, b -> editSearchFocusChanged(b) }

        edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (edSearch.text.toString() == "") {
                    ibClearSearch.visibility = View.GONE
                    resultContainer.visibility = View.GONE
                    hintsContainer.visibility = View.VISIBLE
                } else {
                    runSearch(edSearch.text.toString(), 4)

                    hintsContainer.visibility = View.GONE
                    ibClearSearch.visibility = View.VISIBLE
                    resultContainer.visibility = View.VISIBLE
                    tvShowMore.visibility = View.VISIBLE
                }

            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun runSearch(query: String, limit: Int) {
        resultContainer.visibility = View.VISIBLE
        Thread {
            try {
                val raw = App.connector.getQueryResult(query, limit)
                if (raw.size == 0) return@Thread
                val result = DataFormatter.getStockHolders(raw)
                val adapter = rvResults.adapter as RecyclerResultsAdapter
                adapter.stocks = result
            } catch (e: IndexOutOfBoundsException) {
                Log.e("Activity", "runSearch: IndexOutOfBoundsException ${e.message}")
            }
        }.start()
    }

    private fun setUpHints() {
        hintsContainer.visibility = View.GONE
        rvPopularBubbles.adapter = RecyclerBubblesAdapter(App.getPopularCompany(), this)
        rvSearchedBubbles.adapter = RecyclerBubblesAdapter(App.getSearchedQueries(), this)
        rvPopularBubbles.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        rvSearchedBubbles.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
    }

    private fun setUpResultsPanel() {
        resultContainer.visibility = View.GONE
        rvResults.adapter = RecyclerResultsAdapter(ArrayList(), this)
        tvShowMore.setOnClickListener { showMoreClicked() }
    }

    private fun editSearchFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            isShowMore = false
            edSearch.setHintTextColor(resources.getColor(R.color.colorPrimary))
            ibBackSearch.visibility = View.VISIBLE
            ibSearch.visibility = View.GONE

            viewPager2.visibility = View.GONE
            tabsContainer.visibility = View.GONE
            if (edSearch.text.toString() == "")
                hintsContainer.visibility = View.VISIBLE
        } else {
            edSearch.setHintTextColor(resources.getColor(R.color.colorBlack))

            if (!isShowMore) {
                ibBackSearch.visibility = View.GONE
                ibSearch.visibility = View.VISIBLE
                viewPager2.visibility = View.VISIBLE
                tabsContainer.visibility = View.VISIBLE
                hintsContainer.visibility = View.GONE
            }
        }
    }

    private fun saveQuery() {
        val query = edSearch.text.toString()
        if (query == "")
            return
        Thread {
            try {
                App.getQueryDao().save(UserQuery(query, System.currentTimeMillis()))
            } catch (e: SQLiteConstraintException) {
                App.getQueryDao().updateDate(UserQuery(query, System.currentTimeMillis()))
            }
            (rvSearchedBubbles.adapter as RecyclerBubblesAdapter).apply {
                setSearchedBubbles()
                runOnUiThread { notifyDataSetChanged() }
            }
        }.start()
    }

    private fun changeMenuButtonStyle(isFavor: Boolean) {
        if (isFavor)
            execAnimation(favorMenuButton, stockMenuButton)
        else
            execAnimation(stockMenuButton, favorMenuButton)
    }

    private fun execAnimation(grow: TextView, decrease: TextView) {
        AnimatorInflater.loadAnimator(this, R.animator.tab_animation_decrease)
            .apply {
                setTarget(decrease)
                start()
            }
        AnimatorInflater.loadAnimator(this, R.animator.tab_animation_grow)
            .apply {
                setTarget(grow)
                start()
            }
    }

    private fun setUpPager() {
        val stockPage = StockPage(App.holders, searchContainer, this)
        val favorPage = FavorPage(App.favors, searchContainer, this)

        vpAdapter = StockPagerAdapter(arrayListOf(stockPage, favorPage))
        viewPager2.adapter = vpAdapter

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                changeMenuButtonStyle(viewPager2.currentItem != 0)
                vpAdapter.pageList[position].recAdapter.updateData() // pull new favor
            }
        })

        viewPager2.offscreenPageLimit = 2

        stockMenuButton.setOnClickListener { viewPager2.setCurrentItem(0, true) }
        favorMenuButton.setOnClickListener { viewPager2.setCurrentItem(1, true) }
    }

    private fun searchBackClicked() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(ibBackSearch.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        saveQuery() // hide keyboard

        isShowMore = false
        edSearch.text = "".toEditable()
        edSearch.clearFocus()

        hintsContainer.visibility = View.GONE
        viewPager2.visibility = View.VISIBLE
        ibBackSearch.visibility = View.GONE
        ibSearch.visibility = View.VISIBLE
        tabsContainer.visibility = View.VISIBLE

        (viewPager2.adapter as StockPagerAdapter).pageList[viewPager2.currentItem].recAdapter.updateData() // pull new favors
    }

    private fun searchClearClicked() {
        saveQuery()
        edSearch.text = "".toEditable()
    }

    private fun showMoreClicked() {
        saveQuery()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(tvShowMore.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        isShowMore = true
        tvShowMore.visibility = View.GONE
        edSearch.clearFocus()
        runSearch(edSearch.text.toString(), 20)
    }

    private fun String.toEditable(): Editable? = Editable.Factory.getInstance().newEditable(this)
}

