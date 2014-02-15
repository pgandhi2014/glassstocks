package com.intel.pg.glassstocks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pgandhi on 2/1/14.
 */

public class PortfolioEditActivity extends Activity {
    private static final String TAG = "PortfolioEditActivity";
    private static final int SPEECH_REQUEST = 1224;
    private static final int CARD_REQUEST = 1225;

    private ListView mListView;
    private PortfolioArrayAdapter mArrayAdapter;
    private String mSpeechResults;
    private ArrayList<HashMap<String, String>> mArrayList;
    private DatabaseHandler dbHandler;

    private Stocks s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_edit);
        mListView = (ListView) findViewById(R.id.listView);
        dbHandler = new DatabaseHandler(this);
        mSpeechResults = new String();
        mArrayList = new ArrayList<HashMap<String, String>>();

        mArrayAdapter = new PortfolioArrayAdapter(this, dbHandler.getAllStocks());
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                s = mArrayAdapter.getItem(position);
                openOptionsMenu();
            }
        });
        if (dbHandler.getCount() == 0) {
            DisplaySpeechRecognizer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mArrayAdapter.refreshAdapter(dbHandler.getAllStocks());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, StocksService.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.ticker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.starSymbol:
                if (s.isStarred() == 1) {
                    dbHandler.updateStarredStatus(s.getID(), 0);
                }
                else {
                    if (dbHandler.getStarCount() < 5) {
                    dbHandler.updateStarredStatus(s.getID(), 1);
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.too_many_stars), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                mArrayAdapter.refreshAdapter(dbHandler.getAllStocks());
                return true;
            case R.id.addSymbol:
                DisplaySpeechRecognizer();
                return true;
            case R.id.deleteSymbol:
                dbHandler.deleteStock(s);
                mArrayAdapter.refreshAdapter(dbHandler.getAllStocks());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void DisplaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (!results.isEmpty()) {
                mSpeechResults = results.get(0);
                ProcessSpeechOutput();
            }
            else {
                Log.d(TAG, "nothing returned from recognizer");
            }
        }
        if (requestCode == CARD_REQUEST && resultCode == RESULT_OK) {
            int pos = data.getIntExtra("result",-1);
            if (pos >= 0) {
                HashMap<String, String> map = mArrayList.get(pos);
                Stocks stock = new Stocks(map.get("Symbol"), map.get("Name"), map.get("Exchange"),0);
                dbHandler.addStock(stock);
            }
        }
    }

    private void ProcessSpeechOutput() {
        String[] words = mSpeechResults.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            sb.append(words[i] + "%20");
        }
        new DownloadJSON().execute(sb.toString());
    }

    private void ProcessSearchResults() {
        if (!mArrayList.isEmpty()) {
            Intent intent = new Intent(this, SymbolLookupActivity.class);
            intent.putExtra("arraylist", mArrayList);
            startActivityForResult(intent, CARD_REQUEST);
        }
        else {
            Toast.makeText(this, getResources().getString(R.string.no_matches), Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadJSON extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String input = params[0];
            String urlPrefix = "http://autoc.finance.yahoo.com/autoc?query=%22";
            String urlSuffix = "%22&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
            String url = urlPrefix + input + urlSuffix;
            try {
                mArrayList.clear();
                JSONObject json_data = JSONFunctions.getJSONfromURL(url);
                JSONObject json_resultset = json_data.getJSONObject("ResultSet");
                JSONArray json_results = json_resultset.getJSONArray("Result");

                for (int i = 0; i < json_results.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject symbol = json_results.getJSONObject(i);
                    map.put("Name", symbol.getString("name"));
                    map.put("Symbol", symbol.getString("symbol"));
                    if (symbol.has("exchDisp")) {
                        map.put("Exchange", symbol.getString("exchDisp"));
                    }
                    else {
                        map.put("Exchange", "Unknown");
                    }
                    mArrayList.add(map);
                }
           } catch (JSONException e) {
                Log.e(TAG, "Error getting JSON data " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            ProcessSearchResults();
        }
    }
}
