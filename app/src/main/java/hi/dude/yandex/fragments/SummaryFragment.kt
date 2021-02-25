package hi.dude.yandex.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
            try {
                val summary = App.connector.getSummary(ticker)
                (context as Activity).runOnUiThread { setUpText(summary)  }
            } catch (e: NullPointerException) { // if activity closed before the end of the thread
                Log.e("SummaryFragment", "onViewCreated: ${e.message}")
            }
        }.start()

        cardSite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.data = Uri.parse(tvSite.text.toString())
                startActivity(intent)
            } catch (e: NullPointerException) {
                Log.e("Summary", "onViewCreated: ", e)
            }

        }
    }

    private fun setUpText(summary: Summary) {
        tvDescription.text = DataFormatter.cutDescription(summary.description)
        if (summary.description != null && summary.description.length > 400)
            summaryShowMore.visibility = View.VISIBLE
        else
            summaryShowMore.visibility = View.GONE

        summaryShowMore.setOnClickListener { showMoreClicked(summary) }

//        tvDescription.text = summary.description ?: ""
        tvIndustry.text = summary.industry ?: ""
        tvCeo.text = summary.ceo ?: ""
        tvCountry.text = DataFormatter.getCountryByCode(summary.country)
        tvIpo.text = DataFormatter.toPrettyDate(summary.ipoDate)
        tvSite.text = summary.website ?: ""

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

    private fun showMoreClicked(summary: Summary) {
        summaryShowMore.visibility = View.GONE
        tvDescription.text = summary.description ?: ""
    }
}