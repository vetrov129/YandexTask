package hi.dude.yandex.view.chart

import com.github.mikephil.charting.data.Entry
import hi.dude.yandex.model.entities.ChartLine

class PriceEntry(val x: Int, val pricePoint: ChartLine) :
    Entry(x.toFloat(), if (pricePoint.price == null) 0f else pricePoint.price.toFloat())