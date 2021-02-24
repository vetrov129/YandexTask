package hi.dude.yandex.activities

import android.animation.AnimatorInflater
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.viewpager2.widget.ViewPager2
import hi.dude.yandex.application.App
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.R
import hi.dude.yandex.data.StockHolder
import hi.dude.yandex.adapters.CardPagerAdapter
import kotlinx.android.synthetic.main.action_bar_card_activity.*
import kotlinx.android.synthetic.main.activity_stock_card.*


class StockCardActivity : AppCompatActivity() {

    private lateinit var currentTab: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_card)

        val ticker = intent.getStringExtra("ticker")
        val company = intent.getStringExtra("company")
        val price = intent.getStringExtra("price")
        val change = intent.getStringExtra("change")
        val isFavor = intent.getBooleanExtra("isFavor", false)
        val currency = intent.getStringExtra("currency")
        val image = intent.getParcelableExtra<Bitmap>("image")

        val holder = StockHolder(ticker!!, company, price, isFavor, currency)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.action_bar_card_activity)

        currentTab = tabChart

        tvTickerCard.text = ticker
        tvCompanyCard.text = company

        if (isFavor)
            ibStarCard.setImageResource(R.drawable.ic_black_star)
        else
            ibStarCard.setImageResource(R.drawable.ic_white_star)

        pagerCard.adapter = CardPagerAdapter(supportFragmentManager, lifecycle, holder, change!!, image!!)
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

        ibBackCard.setOnClickListener { onBackPressed() }
        ibStarCard.setOnClickListener { starClicked(holder) }

        tabChart.setOnClickListener { view -> tabClicked(view as TextView) }
        tabSummary.setOnClickListener { view -> tabClicked(view as TextView) }
        tabNews.setOnClickListener { view -> tabClicked(view as TextView) }
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

    private fun starClicked(holder: StockHolder) = Thread {
        val favor = DataFormatter.holderToFavor(holder)

        if (holder.isFavor) {
            App.getFavorDao().delete(favor)
            holder.isFavor = false
            App.removeFavor(holder.ticker)
            ibStarCard.setImageResource(R.drawable.ic_white_star)
        } else {
            App.getFavorDao().save(favor)
            holder.isFavor = true
            App.favors.add(holder)
            ibStarCard.setImageResource(R.drawable.ic_black_star)
        }
    }.start()

    private fun tabClicked(view: TextView) {
        execAnimation(view, currentTab)
        pagerCard.currentItem = when (view) {
            tabChart -> 0
            tabSummary -> 1
            tabNews -> 2
            else -> 0
        }
    }
}





