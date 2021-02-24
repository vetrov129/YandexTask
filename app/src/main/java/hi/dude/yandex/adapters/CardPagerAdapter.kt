package hi.dude.yandex.adapters

import android.graphics.Bitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import hi.dude.yandex.data.StockHolder
import hi.dude.yandex.fragments.ChartFragment
import hi.dude.yandex.fragments.NewsFragment
import hi.dude.yandex.fragments.SummaryFragment

class CardPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val holder: StockHolder,
    val change: String,
    val image: Bitmap
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ChartFragment(holder, change)
        1 -> SummaryFragment(holder.ticker, image)
        2 -> NewsFragment(holder.ticker)
        else -> ChartFragment(holder, change)
    }

}