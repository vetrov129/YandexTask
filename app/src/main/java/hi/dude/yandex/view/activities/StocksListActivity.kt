package hi.dude.yandex.view.activities

import android.animation.AnimatorInflater
import android.content.Context
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
import hi.dude.yandex.R
import hi.dude.yandex.view.adapters.BubblesRecyclerAdapter
import hi.dude.yandex.view.adapters.StocksPagerAdapter
import hi.dude.yandex.view.adapters.StocksRecyclerAdapter
import hi.dude.yandex.view.pages.Page
import hi.dude.yandex.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_stocks_list.*


class StocksListActivity : AppCompatActivity() {

    private lateinit var vpAdapter: StocksPagerAdapter
    private lateinit var viewModel: ListViewModel

    private lateinit var stocksPage: Page
    private lateinit var favorsPage: Page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stocks_list)

        supportActionBar?.hide()
        viewModel = ListViewModel(application)

        setDefaultVisibilityOfSearch()
        viewPager2.visibility = View.GONE
        setUpPager()
        setUpHints()
        setUpSearchPanel()
        setUpResultPanel()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume()
        vpAdapter.pageList[viewPager2.currentItem].recAdapter.notifyDataSetChanged()
    }


    private fun setDefaultVisibilityOfSearch() {
        if (progressBar.visibility == View.GONE) {
            viewPager2.visibility = View.VISIBLE
        }
        edSearch.setHintTextColor(resources.getColor(R.color.colorBlack))
        resultContainer.visibility = View.GONE
        hintsContainer.visibility = View.GONE
        ibBackSearch.visibility = View.GONE
        ibClearSearch.visibility = View.GONE
        ibSearch.visibility = View.VISIBLE
        tabsContainer.visibility = View.VISIBLE
    }

    private fun setEmptyVisibilityOfSearch() {
        if (progressBar.visibility == View.GONE) {
            resultContainer.visibility = View.GONE
            hintsContainer.visibility = View.VISIBLE
        }
        edSearch.setHintTextColor(resources.getColor(R.color.colorPrimary))
        ibBackSearch.visibility = View.VISIBLE
        ibClearSearch.visibility = View.GONE
        ibSearch.visibility = View.GONE
        viewPager2.visibility = View.GONE
        tabsContainer.visibility = View.GONE
    }

    private fun setFilledVisibilityOfSearch() {
        if (progressBar.visibility == View.GONE) {
            resultContainer.visibility = View.VISIBLE
        }
        hintsContainer.visibility = View.GONE
        ibBackSearch.visibility = View.VISIBLE
        ibClearSearch.visibility = View.VISIBLE
        ibSearch.visibility = View.GONE
        viewPager2.visibility = View.GONE
        tabsContainer.visibility = View.GONE
        edSearch.setHintTextColor(resources.getColor(R.color.colorPrimary))
    }

    private fun setUpHints() {
        val bubbleClicked: (String) -> Unit = { edSearch.setText(it) }

        val popularAdapter = BubblesRecyclerAdapter(ArrayList(), bubbleClicked)
        val queryAdapter = BubblesRecyclerAdapter(viewModel.searchedQueries.value ?: ArrayList(), bubbleClicked)
        rvSearchedBubbles.adapter = queryAdapter
        rvPopularBubbles.adapter = popularAdapter

        viewModel.searchedQueries.observe(this) { queryAdapter.bubbles = it }

        rvPopularBubbles.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        rvSearchedBubbles.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
    }

    private fun setUpPages() {
        val starClickedOnStocks: (Int) -> Unit = {
            if (viewModel.checkIsFavor(stocksPage.stocks[it].ticker))
                viewModel.deleteFavor(stocksPage.stocks[it], stocksPage.recAdapter, it)
            else
                viewModel.saveFavor(stocksPage.stocks[it], stocksPage.recAdapter, it)
        }
        val starClickedOnFavors: (Int) -> Unit = {
            viewModel.deleteFavor(favorsPage.stocks[it], favorsPage.recAdapter, it)
        }

        stocksPage = Page(
            viewModel.stocks.value ?: ArrayList(), searchContainer,
            resources, this, viewModel, starClickedOnStocks
        )
        favorsPage = Page(
            ArrayList(), searchContainer,
            resources, this, viewModel, starClickedOnFavors
        )
    }

    private fun setUpPager() {
        setUpPages()
        vpAdapter = StocksPagerAdapter(arrayListOf(stocksPage, favorsPage))
        viewPager2.adapter = vpAdapter
        subscribeStocks()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                changeMenuButtonStyle(viewPager2.currentItem != 0)
                if (viewPager2.currentItem == 0) {
                    stocksPage.recAdapter.notifyDataSetChanged()
                    viewModel.startUpdateStocks(stocksPage)
                } else {
                    viewModel.setFavorHolders(favorsPage)
                    viewModel.startUpdateFavors(favorsPage)
                }
            }
        })

        viewPager2.offscreenPageLimit = 2

        stockMenuButton.setOnClickListener { viewPager2.setCurrentItem(0, true) }
        favorMenuButton.setOnClickListener { viewPager2.setCurrentItem(1, true) }
    }

    private fun subscribeStocks() {
        viewModel.stocks.observe(this) {
            Log.i("ListActivity", "subscribe: stocks")
            stocksPage.stocks = it ?: ArrayList()
        }
        viewModel.favors.observe(this) {
            Log.i("ListActivity", "subscribe: favors")
            viewModel.setFavorHolders(favorsPage)
        }
        viewModel.allStocks.observe(this) {
            viewPager2.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            Log.i("ListActivity", "subscribe: allStocks")
            viewModel.addStocks(stocksPage.recAdapter)
            (rvPopularBubbles.adapter as BubblesRecyclerAdapter).bubbles = viewModel.getPopularCompany()
            viewModel.openWebSocket()
            viewModel.startUpdateStocks(stocksPage)
        }
    }

    private fun changeMenuButtonStyle(isFavor: Boolean) {
        if (isFavor)
            execAnimation(favorMenuButton, stockMenuButton)
        else
            execAnimation(stockMenuButton, favorMenuButton)
    }

    private fun execAnimation(grow: TextView, decrease: TextView) {
        AnimatorInflater.loadAnimator(this, R.animator.tab_decrease)
            .apply {
                setTarget(decrease)
                start()
            }
        AnimatorInflater.loadAnimator(this, R.animator.tab_grow)
            .apply {
                setTarget(grow)
                start()
            }
    }

    private fun setUpSearchPanel() {
        ibClearSearch.setOnClickListener { searchClearClicked() }
        ibBackSearch.setOnClickListener { searchBackClicked() }
        edSearch.setOnFocusChangeListener { _, b -> editSearchFocusChanged(b) }

        edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (edSearch.text.toString() == "") {
                    setEmptyVisibilityOfSearch()
                } else {
                    viewModel.runSearch(edSearch.text.toString())
                    setFilledVisibilityOfSearch()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun setUpResultPanel() {
        val adapter = StocksRecyclerAdapter(viewModel.getResultHolders(),
            resources, this, viewModel) {}

        val starClicked: (Int) -> Unit = {
            if (viewModel.checkIsFavor(adapter.stocks[it].ticker))
                viewModel.deleteFavor(adapter.stocks[it], adapter, it)
            else
                viewModel.saveFavor(adapter.stocks[it], adapter, it)
        }

        adapter.starClicked = starClicked

        rvResults.adapter = adapter
        tvShowMore.setOnClickListener { showMoreClicked() }
        viewModel.queryResults.observe(this) {
            adapter.stocks = viewModel.getResultHolders()
            viewModel.pullHolderData(0, adapter.stocks.size, adapter, adapter.stocks)
        }
    }

    private fun searchBackClicked() {
        // hide keyboard
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(ibBackSearch.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        viewModel.saveQuery(edSearch.text.toString())
        viewModel.clearQueryResults()
        edSearch.text = "".toEditable()
        edSearch.clearFocus()
        setDefaultVisibilityOfSearch()
        viewModel.clearQueryResults()
    }

    private fun searchClearClicked() {
        viewModel.saveQuery(edSearch.text.toString())
        edSearch.text = "".toEditable()
    }

    private fun showMoreClicked() {
        viewModel.saveQuery(edSearch.text.toString())
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(ibBackSearch.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        viewModel.runSearch(edSearch.text.toString(), 20)
    }

    private fun editSearchFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            if (edSearch.text.toString() == "")
                setEmptyVisibilityOfSearch()
            else
                setFilledVisibilityOfSearch()
        } else {
            setDefaultVisibilityOfSearch()
        }
    }

    private fun String.toEditable(): Editable? = Editable.Factory.getInstance().newEditable(this)
}

