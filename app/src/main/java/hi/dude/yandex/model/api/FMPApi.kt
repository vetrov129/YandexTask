package hi.dude.yandex.model.api

import hi.dude.yandex.model.entities.Quote
import hi.dude.yandex.model.entities.Stock
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FMPApi {
    @GET("api/v3/quote/{ticker}?")
    fun getQuote(
        @Path("ticker") ticker: String,
        @Query("apikey") apikey: String
    ): Call<Quote>

    @GET("api/v3/stock-screener?")
    fun getStocks(
        @Query("apikey") apikey: String,
        @Query("exchange") exchange: String = "nasdaq"
    ): Call<ArrayList<Stock>>
}

//https://financialmodelingprep.com/api/v3/quote/AAPL?apikey=demo