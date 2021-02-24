package hi.dude.yandex.chart

import com.github.mikephil.charting.data.Entry
import hi.dude.yandex.data.api.models.ChartLine

class PriceEntry(val x: Int, val pricePoint: ChartLine) :
    Entry(x.toFloat(), if (pricePoint.price == null) 0f else pricePoint.price.toFloat())