package hi.dude.yandex.view.activities

import android.animation.AnimatorInflater
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.viewpager2.widget.ViewPager2
import hi.dude.yandex.R
import hi.dude.yandex.view.adapters.CardPagerAdapter
import hi.dude.yandex.viewmodel.CardViewModel
import hi.dude.yandex.viewmodel.StockHolder
import kotlinx.android.synthetic.main.action_bar_card_activity.*
import kotlinx.android.synthetic.main.activity_stock_card.*


class StockCardActivity : AppCompatActivity() {

    private lateinit var currentTab: TextView
    lateinit var viewModel: CardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_card)

        viewModel = CardViewModel(application)
        val holder = getHolder()

        viewModel.clearCardData()
        viewModel.pullChartData(holder.ticker)
        viewModel.pullSummary(holder.ticker)
        viewModel.pullNews(holder.ticker)

        setUpActionBar(holder)
        setUpStar(holder)
        setUpPager(holder)
        setUpTabs()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume()
    }

    private fun getHolder(): StockHolder {
        val ticker = intent.getStringExtra("ticker")
        val company = intent.getStringExtra("company")
        val price = intent.getStringExtra("price")
        val change = intent.getStringExtra("change")
        val currency = intent.getStringExtra("currency")

        val holder = StockHolder(ticker!!, company, price, currency)
        holder.change = change ?: ""
        return holder
    }

    private fun setUpPager(holder: StockHolder) {
        pagerCard.adapter = CardPagerAdapter(supportFragmentManager, lifecycle, holder, viewModel)
        pagerCard.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> execAnimation(tabChart, currentTab)
                    1 -> execAnimation(tabSummary, currentTab)
                    2 -> execAnimation(tabNews, currentTab)
                }
            }
        })
    }

    private fun setUpTabs() {
        currentTab = tabChart
        tabChart.setOnClickListener { view -> tabClicked(view as TextView) }
        tabSummary.setOnClickListener { view -> tabClicked(view as TextView) }
        tabNews.setOnClickListener { view -> tabClicked(view as TextView) }
    }

    private fun tabClicked(view: TextView) {
        execAnimation(view, currentTab)
        pagerCard.currentItem = when (view) {
            tabChart -> 0
            tabSummary -> 1
            tabNews -> 2
            else -> 0
        }
    }

    private fun setUpActionBar(holder: StockHolder) {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.action_bar_card_activity)

        tvTickerCard.text = holder.ticker
        tvCompanyCard.text = holder.company
        ibBackCard.setOnClickListener { onBackPressed() }
    }

    private fun execAnimation(grow: TextView, decrease: TextView) {
        AnimatorInflater.loadAnimator(this, R.animator.tab_animation_decrease_small)
            .apply {
                setTarget(decrease)
                start()
            }
        AnimatorInflater.loadAnimator(this, R.animator.tab_animation_grow_small)
            .apply {
                setTarget(grow)
                start()
            }
        currentTab = grow
    }

    private fun setUpStar(holder: StockHolder) {
        if (viewModel.checkIsFavor(holder.ticker))
            ibStarCard.setImageResource(R.drawable.ic_black_star)
        else
            ibStarCard.setImageResource(R.drawable.ic_white_star)

        ibStarCard.setOnClickListener {
            if (viewModel.checkIsFavor(holder.ticker)) {
                viewModel.deleteFavor(holder)
                ibStarCard.setImageResource(R.drawable.ic_white_star)
            } else {
                viewModel.saveFavor(holder)
                ibStarCard.setImageResource(R.drawable.ic_black_star)
            }
        }
    }
}





