package hi.dude.yandex.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import hi.dude.yandex.*
import hi.dude.yandex.application.App
import hi.dude.yandex.chart.PointMarker
import hi.dude.yandex.chart.PriceEntry
import hi.dude.yandex.data.api.models.ChartLine
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.data.StockHolder
import kotlinx.android.synthetic.main.fragment_chart.*

class ChartFragment(private val holder: StockHolder, val change: String) : Fragment() {

    private lateinit var buttonSelected: TextView

    private var pricesAll: ArrayList<ChartLine>? = null
    private var pricesYear: ArrayList<ChartLine>? = null
    private var pricesSixMonth: ArrayList<ChartLine>? = null
    private var pricesMonth: ArrayList<ChartLine>? = null
    private var pricesWeek: ArrayList<ChartLine>? = null
    private var pricesDay: ArrayList<ChartLine>? = null

    private var currentPrices = ArrayList<ChartLine>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pullFirst()
        pullPrices()
        setUpBalloon()
        setUpChart()
        setUpText()
        setChartData(currentPrices)
        buttonSelected = tvWeek
        setUpPeriodButtons()

        buttonBuyFor.setOnClickListener {
            Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pullFirst() = Thread {
        pricesWeek = App.connector.getWeekChartData(holder.ticker)
        if (chart.data.getDataSetByLabel("", false).entryCount == 0) {
            setChartData(pricesWeek)
            currentPrices = pricesWeek!!
            chart.invalidate()
        }
    }.start()

    private fun setUpPeriodButtons() {
        tvDay.setOnClickListener { view ->
            periodClicked(view as TextView, pricesDay)
            { ticker -> App.connector.getDayChartData(ticker) }
        }
        tvWeek.setOnClickListener { view ->
            periodClicked(view as TextView, pricesWeek)
            { ticker -> App.connector.getWeekChartData(ticker) }
        }
        tvMonth.setOnClickListener { view ->
            periodClicked(view as TextView, pricesMonth)
            { ticker -> App.connector.getMonthChartData(ticker) }
        }
        tvSixMonth.setOnClickListener { view ->
            periodClicked(view as TextView, pricesSixMonth)
            { ticker -> App.connector.getSixMonthChartData(ticker) }
        }
        tvYear.setOnClickListener { view ->
            periodClicked(view as TextView, pricesYear)
            { ticker -> App.connector.getYearChartData(ticker) }
        }
        tvAll.setOnClickListener { view ->
            periodClicked(view as TextView, pricesAll)
            { ticker -> App.connector.getAllChartData(ticker) }
        }
    }

    private fun setUpText() {
        tvPriceChart.text = holder.price
        tvChangeChart.text = change
        if (change != "" && change[0] == '-')
            tvChangeChart.setTextColor(resources.getColor(R.color.colorRed))
        else
            tvChangeChart.setTextColor(resources.getColor(R.color.colorGreen))
        buttonBuyFor.text = "${buttonBuyFor.text} ${holder.price}"
    }

    private fun periodClicked(view: TextView, list: ArrayList<ChartLine>?, getter: (String) -> ArrayList<ChartLine>) {
        buttonSelected.setBackgroundResource(R.drawable.shape_period_not_selected_background)
        buttonSelected.setTextColor(resources.getColor(R.color.colorBlack))

        view.setBackgroundResource(R.drawable.shape_period_selected_background)
        view.setTextColor(resources.getColor(R.color.colorPrimary))

        buttonSelected = view
        balloonArrow.visibility = View.GONE
        balloonTop.visibility = View.GONE
        waitDataAndUpdate(list, getter) // if the data did not have time to load, it will be loaded out of turn
    }

    private fun waitDataAndUpdate(list: ArrayList<ChartLine>?, getter: (String) -> ArrayList<ChartLine>) {
        var prises = ArrayList<ChartLine>()
        if (list == null) {
            Thread { prises = getter(holder.ticker) }.apply {
                start()
                join()
            }
        } else prises = list
        setChartData(prises)
        chart.invalidate()
        currentPrices = prises
    }

    private fun pullPrices() = Thread {
        if (pricesDay == null) pricesDay = App.connector.getDayChartData(holder.ticker)
//        pricesWeek = App.connector.getWeekChartData(holder.ticker)     // received at start
        if (pricesMonth == null) pricesMonth = App.connector.getMonthChartData(holder.ticker)
        if (pricesSixMonth == null) pricesSixMonth = App.connector.getSixMonthChartData(holder.ticker)
        if (pricesYear == null) pricesYear = App.connector.getYearChartData(holder.ticker)
        if (pricesAll == null) pricesAll = App.connector.getAllChartData(holder.ticker)
    }.start()

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
        set.fillFormatter = IFillFormatter { _, _ -> chart.axisLeft.axisMinimum }
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

        val dp80 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics)
        val dp6 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics)
        // move main pixel to new place
        val params = ConstraintLayout.LayoutParams(1, 1)
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.setMargins(x.toInt() + dp6.toInt(), y.toInt() + dp80.toInt(), 0, 0)
        mainPixel.layoutParams = params

        balloonTop.visibility = View.VISIBLE
        balloonArrow.visibility = View.VISIBLE
    }
}