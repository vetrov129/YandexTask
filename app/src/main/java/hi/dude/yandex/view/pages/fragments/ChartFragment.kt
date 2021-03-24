package hi.dude.yandex.view.pages.fragments

import android.animation.AnimatorInflater
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import hi.dude.yandex.*
import hi.dude.yandex.model.entities.ChartLine
import hi.dude.yandex.view.chart.PointMarker
import hi.dude.yandex.view.chart.PriceEntry
import hi.dude.yandex.viewmodel.CardViewModel
import hi.dude.yandex.viewmodel.DataFormatter
import hi.dude.yandex.viewmodel.StockHolder
import kotlinx.android.synthetic.main.fragment_chart.*

class ChartFragment(private val holder: StockHolder, val viewModel: CardViewModel) : Fragment() {

    private lateinit var buttonSelected: TextView
    private val prices = HashMap<TextView, LiveData<ArrayList<ChartLine>>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buttonSelected = tvAll

        setUpBalloon()
        setUpChart()
        setUpPriceText(holder.price, holder.change)
        subscribePrice()
        setUpChartPrices()
        subscribeChartLists()
        setChartData(prices[buttonSelected]?.value)
        setUpPeriodButtons()

        buttonBuyFor.setOnClickListener {
            Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subscribePrice() {
        viewModel.realTimePrice.observe(this) {
            if (it != null) {
                setUpPriceText(
                    DataFormatter.addCurrency(it.price, holder.currency, true),
                    DataFormatter.getChange(it.price, holder.priceClose, holder.currency)
                )
                when {
                    it.price!! > holder.priceDouble!! -> {
                        AnimatorInflater.loadAnimator(context, R.animator.grow_price)
                            .apply {
                                setTarget(tvPriceChart)
                                start()
                            }
                    }
                    it.price < holder.priceDouble!! -> {
                        AnimatorInflater.loadAnimator(context, R.animator.decrease_price)
                            .apply {
                                setTarget(tvPriceChart)
                                start()
                            }
                    }
                }
                holder.priceDouble = it.price
            }
            Log.i("ChartFragment", "updateText: price ${it?.price} closePrice ${holder.priceClose}")
        }
    }

    private fun setUpChartPrices() {
        prices[tvDay] = viewModel.dayChart
        prices[tvWeek] = viewModel.weekChart
        prices[tvMonth] = viewModel.monthChart
        prices[tvSixMonth] = viewModel.sixMonthChart
        prices[tvYear] = viewModel.yearChart
        prices[tvAll] = viewModel.allTimeChart
    }

    private fun subscribeChartLists() {
        val updateAction: (ArrayList<ChartLine>?) -> Unit = {
            if (prices[buttonSelected]?.value == it) {
                setChartData(it)
            }
        }
        viewModel.dayChart.observe(this, updateAction)
        viewModel.weekChart.observe(this, updateAction)
        viewModel.monthChart.observe(this, updateAction)
        viewModel.sixMonthChart.observe(this, updateAction)
        viewModel.yearChart.observe(this, updateAction)
        viewModel.allTimeChart.observe(this, updateAction)
    }

    private fun setUpPeriodButtons() {
        tvDay.setOnClickListener { v -> periodClicked(v as TextView) }
        tvWeek.setOnClickListener { v -> periodClicked(v as TextView) }
        tvMonth.setOnClickListener { v -> periodClicked(v as TextView) }
        tvSixMonth.setOnClickListener { v -> periodClicked(v as TextView) }
        tvYear.setOnClickListener { v -> periodClicked(v as TextView) }
        tvAll.setOnClickListener { v -> periodClicked(v as TextView) }
    }

    private fun setUpPriceText(price: String, change: String) {
        tvPriceChart.text = price
        tvChangeChart.text = change
        if (change != "" && change[0] == '-')
            tvChangeChart.setTextColor(resources.getColor(R.color.colorRed))
        else
            tvChangeChart.setTextColor(resources.getColor(R.color.colorGreen))
        buttonBuyFor.text = "${resources.getString(R.string.buy_for)} $price"
    }

    private fun periodClicked(view: TextView) {
        buttonSelected.setBackgroundResource(R.drawable.shape_period_not_selected_background)
        buttonSelected.setTextColor(resources.getColor(R.color.colorBlack))

        view.setBackgroundResource(R.drawable.shape_period_selected_background)
        view.setTextColor(resources.getColor(R.color.colorPrimary))

        buttonSelected = view
        balloonArrow.visibility = View.GONE
        balloonTop.visibility = View.GONE
        chart.setDrawMarkers(false)

        setChartData(prices[view]?.value)
    }

    private fun setUpChart() {
        chart.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.isEnabled = false
        chart.legend.isEnabled = false
        chart.setViewPortOffsets(0f, 0f, 0f, 0f)
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                chart.setDrawMarkers(true)
                valueClicked(e as PriceEntry)
            }

            override fun onNothingSelected() {
                balloonTop.visibility = View.GONE
                balloonArrow.visibility = View.GONE
            }
        })

        chart.marker = PointMarker(context!!)
    }

    private fun setUpDataSetGraphics(set: LineDataSet) {
        set.color = Color.BLACK
        set.lineWidth = 2f
        set.setDrawCircleHole(false)
        set.setDrawFilled(true)
        set.fillDrawable = getDrawable(this.context!!, R.drawable.chart_gradient)
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.setDrawIcons(false)
        set.setDrawHighlightIndicators(false)
        set.setDrawHorizontalHighlightIndicator(false)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
    }

    private fun setChartData(prices: ArrayList<ChartLine>?) {
        if (prices == null) return
        val values = ArrayList<Entry>()
        var x = 0
        for (i in prices.size - 1 downTo 0) // list of price points has reverse order by date
            values.add(PriceEntry(x++, prices[i]))

        val set = LineDataSet(values, "")
        setUpDataSetGraphics(set)
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set)
        val lineData = LineData(dataSets)
        chart.data = lineData
        chart.invalidate()
    }

    private fun setUpBalloon() {
        balloonTop.visibility = View.GONE
        balloonArrow.visibility = View.GONE

        balloonTop.setOnClickListener {
            balloonTop.visibility = View.GONE
            balloonArrow.visibility = View.GONE
        }

        balloonArrow.setOnClickListener {
            balloonTop.visibility = View.GONE
            balloonArrow.visibility = View.GONE
        }
    }

    private fun valueClicked(entry: PriceEntry) { // calculates the margins and places the balloon in the right place
        // set new text from current point
        balloonPrice.text = DataFormatter.addCurrency(entry.pricePoint.price, holder.currency, true)
        balloonDate.text = DataFormatter.toPrettyDate(entry.pricePoint.date)
        // selected coordinates
        val x = chart.getPixelForValues(entry.x.toFloat(), entry.y, chart.axisLeft.axisDependency).x
        val y = chart.getPixelForValues(entry.x.toFloat(), entry.y, chart.axisLeft.axisDependency).y
        // margin of chart
        val dp80 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics)
        // move main pixel to new place
        val params = ConstraintLayout.LayoutParams(1, 1)
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.setMargins(x.toInt(), y.toInt() + dp80.toInt(), 0, 0)
        mainPixel.layoutParams = params

        balloonTop.visibility = View.VISIBLE
        balloonArrow.visibility = View.VISIBLE
    }
}