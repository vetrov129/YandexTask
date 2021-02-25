package hi.dude.yandex.chart

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.utils.MPPointF
import hi.dude.yandex.R

class PointMarker(context: Context): MarkerView(context, R.layout.marker) {

    override fun getOffset(): MPPointF {
        return MPPointF(-(width.toFloat() / 2), -(height.toFloat() / 2))
    }
}