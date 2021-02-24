package hi.dude.yandex.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hi.dude.yandex.application.App
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.R
import hi.dude.yandex.data.api.models.Summary
import kotlinx.android.synthetic.main.fragment_summary.*

class SummaryFragment(val ticker: String, val image: Bitmap): Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextVisibility(View.GONE)
        Thread {
            val summary = App.connector.getSummary(ticker)
            (context as Activity).runOnUiThread { setUpText(summary)  }
        }.start()

        cardSite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(tvSite.text.toString())
            startActivity(intent)
        }
    }

    private fun setUpText(summary: Summary) {
        tvDescription.text = summary.description
        tvIndustry.text = summary.industry
        tvCeo.text = summary.ceo
        tvCountry.text = DataFormatter.getCountryByCode(summary.country)
        tvIpo.text = DataFormatter.toPrettyDate(summary.ipoDate)
        tvSite.text = summary.website

        setTextVisibility(View.VISIBLE)
    }

    private fun setTextVisibility(visibility: Int) {
        tvDescription.visibility = visibility
        cardIndustry.visibility = visibility
        cardCeo.visibility = visibility
        cardCountry.visibility = visibility
        cardIpo.visibility = visibility
        cardSite.visibility = visibility
    }

}