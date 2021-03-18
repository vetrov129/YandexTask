package hi.dude.yandex.view.activities

import android.animation.AnimatorInflater
import android.os.Bundle
import android.text.Editable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import hi.dude.yandex.R
import hi.dude.yandex.application.App
import hi.dude.yandex.view.adapters.StocksPagerAdapter
import hi.dude.yandex.view.pages.Page
import hi.dude.yandex.viewmodel.StockViewModel
import kotlinx.android.synthetic.main.activity_stocks_list.*


class StocksListActivity : AppCompatActivity() {

    private lateinit var vpAdapter: StocksPagerAdapter
    private lateinit var viewModel: StockViewModel

    private lateinit var stocksPage: Page
    private lateinit var favorsPage: Page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stocks_list)

        supportActionBar?.hide()
        viewModel = StockViewModel(application)

        setUpPager()
    }

    private fun setUpPager() {
        val starClicked: (Int) -> Unit = {}

        stocksPage = Page(
            viewModel.stocks.value ?: ArrayList(),
            searchContainer, resources, this, viewModel, starClicked
        )
        favorsPage = Page(
            viewModel.favors.value ?: ArrayList(),
            searchContainer, resources, this, viewModel, starClicked
        )

        vpAdapter = StocksPagerAdapter(arrayListOf(stocksPage, favorsPage))
        viewPager2.adapter = vpAdapter
        subscribe()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                changeMenuButtonStyle(viewPager2.currentItem != 0)
                if (viewPager2.currentItem == 0) {
                    viewModel.pullStocks()
                    viewModel.updateStar(stocksPage.recAdapter)
                } else {
                    viewModel.pullFavors()
                }
            }
        })

        viewPager2.offscreenPageLimit = 2

        stockMenuButton.setOnClickListener { viewPager2.setCurrentItem(0, true) }
        favorMenuButton.setOnClickListener { viewPager2.setCurrentItem(1, true) }
    }

    private fun subscribe() {
        viewModel.stocks.observe(this) { stocksPage.recAdapter.stocks = it }
        viewModel.favors.observe(this) { favorsPage.recAdapter.stocks = it }
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

    private fun String.toEditable(): Editable? = Editable.Factory.getInstance().newEditable(this)
}

