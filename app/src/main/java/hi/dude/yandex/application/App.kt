package hi.dude.yandex.application

import android.app.Application
import android.util.Log
import androidx.room.Room
import hi.dude.yandex.data.api.ApiConnector
import hi.dude.yandex.data.api.models.Stock
import hi.dude.yandex.data.DataFormatter
import hi.dude.yandex.data.StockHolder
import hi.dude.yandex.data.room.DaoGetter
import hi.dude.yandex.data.room.FavorStockDao
import hi.dude.yandex.data.room.QueryDao

class App : Application() {

    companion object {
        lateinit var connector: ApiConnector
            private set

        lateinit var stocks: ArrayList<Stock>  // full list of stocks
        lateinit var favors: ArrayList<StockHolder>
        lateinit var holders: ArrayList<StockHolder> // list os stocks on main page
        private lateinit var daoGetter: DaoGetter

        fun getFavorDao(): FavorStockDao {
            return daoGetter.getFavorStockDao()
        }

        fun getQueryDao(): QueryDao {
            return daoGetter.getQueryDao()
        }

        fun updateFavor(actionAfter: () -> Unit): ArrayList<StockHolder> {
            favors = DataFormatter.getStockHolders(getFavorDao().getAll())
            actionAfter()
            return favors
        }

        fun checkIsFavor(ticker: String): Boolean {
            return favors.any { it.ticker == ticker }
        } // TODO: 25.02.2021 сделать HashSet по тикеру

        fun updateStars() {
            holders.forEach { it.isFavor = checkIsFavor(it.ticker) }
        }

        fun getPopularCompany(count: Int = 20): ArrayList<String> {
            val companies = ArrayList<String>()
            for (i in 0 until count) {
                companies.add(DataFormatter.companyToQuery(stocks[i].company))
            }
            return companies
        }

        fun getSearchedQueries(count: Int = 30): List<String> {
            var companies: List<String> = ArrayList()
            val thread = Thread {
                companies = getQueryDao().getStrings(count)
            }
            thread.start()
            thread.join()
            return companies
        }

        fun removeFavor(ticker: String) {
            favors.removeIf { it.ticker == ticker }
        }
    }

    override fun onCreate() {
        super.onCreate()
        connector = ApiConnector()
        val thread = Thread {
            stocks = connector.getStockList()
            stocks.add(0, Stock("YNDX", "Yandex N.V.", 0.0, "US"))
            daoGetter = Room.databaseBuilder(applicationContext, DaoGetter::class.java, "stocks.sqlite").build()
            favors = DataFormatter.getStockHolders(getFavorDao().getAll())
            holders = DataFormatter.getStockHolders(stocks)
        }
        thread.start()
        thread.join()
    }
}