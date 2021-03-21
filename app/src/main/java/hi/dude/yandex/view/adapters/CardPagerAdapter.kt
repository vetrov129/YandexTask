package hi.dude.yandex.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import hi.dude.yandex.fragments.NewsFragment
import hi.dude.yandex.view.pages.fragments.SummaryFragment
import hi.dude.yandex.view.pages.fragments.ChartFragment
import hi.dude.yandex.viewmodel.CardViewModel
import hi.dude.yandex.viewmodel.StockHolder

class CardPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val holder: StockHolder,
    val viewModel: CardViewModel
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment  = when (position) {
        0 -> ChartFragment(holder, viewModel)
        1 -> SummaryFragment(holder.ticker, viewModel)
        2 -> NewsFragment(holder.ticker, viewModel)
        else -> ChartFragment(holder, viewModel)
    }
}