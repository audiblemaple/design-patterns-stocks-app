package com.example.tradingview

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tradingview.APIAdapter.APIAdapter
import com.example.tradingview.APIAdapter.APIAdapter.SymbolsListCallback
import com.example.tradingview.SingletonFileManager.SingletonFileManager
import org.json.JSONArray
import java.util.Locale


class MainActivity : ComponentActivity() {
    private var currencyRV: RecyclerView? = null
    private var searchEdt: EditText? = null
    private var currencyModalArrayList: ArrayList<CurrencyModal>? = null
    private var currencyRVAdapter: CurrencyRVAdapter? = null
    private var loadingPB: ProgressBar? = null
    private var apiAdapter: APIAdapter = APIAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure you have activity_main.xml in your layout resources

        val openStocksListButton: Button = findViewById(R.id.idOpenStocksList)
        openStocksListButton.setOnClickListener {
            val intent = Intent(this@MainActivity, StocksList::class.java)
            startActivity(intent)
        }

        val content = SingletonFileManager.getInstance().readFile(this)

        searchEdt = findViewById(R.id.idEdtCurrency)
        loadingPB = findViewById(R.id.idPBLoading)
        currencyRV = findViewById(R.id.idRVcurrency)
        currencyModalArrayList = ArrayList()
        currencyRVAdapter = CurrencyRVAdapter(this, currencyModalArrayList, object : CurrencyRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Intent to start your GraphViewerActivity
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(intent)
            }
        })


//        populateList()

        searchEdt?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })
    }

    private fun filter(text: String) {
        val searchTextLowercase = text.lowercase(Locale.getDefault())
        val exactMatches = ArrayList<CurrencyModal>()
        val partialMatches = ArrayList<CurrencyModal>()

        currencyModalArrayList?.forEach {
            when {
                // Check for exact match in symbol
                it.symbol.lowercase(Locale.getDefault()) == searchTextLowercase -> exactMatches.add(it)
                // Check for exact match in name
                it.name.lowercase(Locale.getDefault()) == searchTextLowercase -> exactMatches.add(it)
                // Check for partial match in symbol or name
                it.symbol.lowercase(Locale.getDefault()).contains(searchTextLowercase) ||
                        it.name.lowercase(Locale.getDefault()).contains(searchTextLowercase) -> partialMatches.add(it)
            }
        }
        val filteredList = ArrayList<CurrencyModal>().apply {
            addAll(exactMatches)
            addAll(partialMatches)
        }

        if (filteredList.isEmpty())
            Toast.makeText(this, "No currency found.", Toast.LENGTH_SHORT).show()
        else
            currencyRVAdapter?.filterList(filteredList)
        currencyRVAdapter?.refresh()
    }

    private fun populateList() {
        apiAdapter.getSymbolsList(object : SymbolsListCallback {
            override fun onSuccess(symbolsList: JSONArray) {
                for (i in 0 until symbolsList.length()) {
                    val item = symbolsList.getJSONObject(i)
                    val symbol = item.getString("symbol")
                    val name = item.getString("name")
                    val price = 0.00 // Use empty string for price
                    val percentChange24h = 0.00 // Use empty string for percent_change_24h

                    currencyModalArrayList?.add(CurrencyModal(name, symbol, price, percentChange24h))
                    if ((i + 1) % 10 == 0 || i == symbolsList.length() - 1) {
                        currencyRVAdapter?.refresh()
                    }
                }
            }

            override fun onError(errorMessage: String) {
                // Handle the error here, for example, print error message to console
                System.err.println(errorMessage)
            }
        })
    }
}


//    private fun getData() {
//        val url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest"
//
//        val queue: RequestQueue = Volley.newRequestQueue(this)
//
//        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null,
//            Response.Listener { response ->
//                loadingPB?.visibility = View.GONE
//                try {
//                    val dataArray = response.getJSONArray("data")
////                    val filteredSymbols = listOf("BTC", "ETH", "XRP", "SOL")
//                    for (i in 0 until dataArray.length()) {
//                        val dataObj = dataArray.getJSONObject(i)
//                        val symbol = dataObj.getString("symbol")
////                        if (symbol in filteredSymbols) { // Check if the symbol is one of the filtered ones
//                            val name = dataObj.getString("name")
//                            val quote = dataObj.getJSONObject("quote")
//                            val USD = quote.getJSONObject("USD")
//                            val price = USD.getDouble("price")
//                            val percent_change_24h = USD.getDouble("percent_change_24h") // Get the volume change
//                            currencyModalArrayList?.add(CurrencyModal(name, symbol, price, percent_change_24h)) // Pass it to the constructor
////                        }
//                    }
//                    currencyRVAdapter?.notifyDataSetChanged()
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                    Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
//                }
//            },
//            Response.ErrorListener { error ->
//                Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
//            }) {
//            override fun getHeaders(): Map<String, String> {
//                val headers = HashMap<String, String>()
//                headers["X-CMC_PRO_API_KEY"] = "335008f7-957b-4453-9595-1cbfdf377afd" // Make sure to use your actual API key
//                return headers
//            }
//        }
//        queue.add(jsonObjectRequest)
//    }