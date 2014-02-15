package com.intel.pg.glassstocks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pgandhi on 2/9/14.
 */

public class ViewAllActivity extends Activity {
    private static final String TAG = "ViewAllActivity";

    private ListView mListView;
    private ViewAllArrayAdapter mArrayAdapter;
    private DatabaseHandler dbHandler;
    private List<Stocks> listStocks = new ArrayList<Stocks>();
    private String stringStocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);
        mListView = (ListView) findViewById(R.id.listViewAll);
        dbHandler = new DatabaseHandler(this);
        listStocks = dbHandler.getAllStocks();
        stringStocks = createStringList();
        fetchQuotes();
        mArrayAdapter = new ViewAllArrayAdapter(this, listStocks);
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                openOptionsMenu();
            }
        });
    }

    private void fetchQuotes() {
        if (stringStocks != null) {
            new DownloadJSON().execute(stringStocks);
        }
    }

    private String createStringList() {
        if (listStocks.isEmpty()) {
            Log.d(TAG, "No stocks in the list");
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Stocks stock : listStocks) {
            String s = stock.getSymbol();
            if (s.contains("^")) {
                sb.append("%22" + s.replace("^", "%5E") + "%22%2C");
            }
            else {
                sb.append("%22" + s + "%22%2C");
            }
        }
        int pos = sb.lastIndexOf("%2C");
        if (pos > 0) {
            sb.delete(pos,pos+3);
        }
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mArrayAdapter.refreshAdapter(listStocks);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, StocksService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_all, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            fetchQuotes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // DownloadJSON AsyncTask
    private class DownloadJSON extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String input = params[0];
            String urlPrefix = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(";
            String urlSuffix = ")&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
            String url =  urlPrefix + input + urlSuffix;
            JSONArray json_quotes = new JSONArray();
            JSONObject json_results = new JSONObject();
            try {
                JSONObject json_data = JSONFunctions.getJSONfromURL(url);
                JSONObject json_query = json_data.getJSONObject("query");
                json_results = json_query.getJSONObject("results");
                json_quotes = json_results.getJSONArray("quote");
            }catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                try {
                    JSONObject json_quote = json_results.getJSONObject("quote");
                    json_quotes.put(json_quote);
                } catch (JSONException e1) {
                    Log.e(TAG, e1.getMessage());
                }
            }
            try {
                for (int i = 0; i < json_quotes.length(); i++) {
                    Stocks s = listStocks.get(i);
                    JSONObject quote = json_quotes.getJSONObject(i);
                    if (s.getSymbol().equalsIgnoreCase(quote.getString("symbol"))) {
                        s.setLastTradePrice(quote.getString("LastTradePriceOnly"));
                        s.setLastTradeChange(quote.getString("Change"));
                        dbHandler.updateLastTrade(s.getID(), s.getLastTradePrice(), s.getLastTradeChange());
                    }
                    else {
                        Log.d(TAG, "Could not match stock objects");
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            mArrayAdapter.refreshAdapter(listStocks);
        }
    }

}
