package com.intel.pg.glassstocks;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pgandhi on 1/26/14.
 * Service owning the LiveCard living in the timeline.
 */
public class StocksService extends Service {
    private static final String TAG = "StocksService";
    private static final String LIVE_CARD_TAG = "stocks";

    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;
    RemoteViews mRemoteViews;
    private DatabaseHandler dbHandler;
    private List<Stocks> listStocks = new ArrayList<Stocks>();

    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
        mRemoteViews = new RemoteViews(getPackageName(),R.layout.card_stocks);
        dbHandler = new DatabaseHandler(this);
    }


    private void FetchQuotes() {
        listStocks = dbHandler.getStarredStocks();
        if (listStocks.isEmpty()) {
            Log.d(TAG, "No stocks in the list");
            refreshDisplay();
            //Intent intent = new Intent(this, PortfolioEditActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //startActivity(intent);
            Toast.makeText(this, getResources().getString(R.string.portfolio_empty), Toast.LENGTH_SHORT).show();
            return;
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
        new DownloadJSON().execute(sb.toString());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
            mLiveCard.setViews(mRemoteViews);

            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.publish(PublishMode.REVEAL);
        }
        FetchQuotes();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    public void refreshDisplay() {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        int i = 1;
        int layoutID = 0;
        for (Stocks s : listStocks) {
            layoutID = getResources().getIdentifier("textViewSymbol"+i, "id", getPackageName());
            mRemoteViews.setTextViewText(layoutID, s.getSymbol());
            layoutID = getResources().getIdentifier("textViewValue"+i, "id", getPackageName());
            mRemoteViews.setTextViewText(layoutID, s.getLastTradePrice());
            layoutID = getResources().getIdentifier("textViewChange"+i, "id", getPackageName());
            mRemoteViews.setTextViewText(layoutID, s.getLastTradeChange());
            if (s.getLastTradeChange() != null) {
                if (s.getLastTradeChange().startsWith("+")) {
                    mRemoteViews.setInt(layoutID,"setBackgroundColor", getResources().getColor(R.color.green));
                }
                else {
                    mRemoteViews.setInt(layoutID,"setBackgroundColor", getResources().getColor(R.color.red));
                }
            }
            i++;
        }
        while (i < 6) {
            layoutID = getResources().getIdentifier("textViewSymbol"+i, "id", getPackageName());
            mRemoteViews.setTextViewText(layoutID, "");
            layoutID = getResources().getIdentifier("textViewValue"+i, "id", getPackageName());
            mRemoteViews.setTextViewText(layoutID, "");
            layoutID = getResources().getIdentifier("textViewChange"+i, "id", getPackageName());
            mRemoteViews.setTextViewText(layoutID, "");
            mRemoteViews.setInt(layoutID,"setBackgroundColor", getResources().getColor(R.color.black));
            i++;
        }
        mRemoteViews.setTextViewText(R.id.textViewUpdated, "Last updated " + currentDateTimeString);
        mLiveCard.setViews(mRemoteViews);
    }

    // DownloadJSON AsyncTask
    private class DownloadJSON extends AsyncTask<String, Void, Void> {
        private ArrayList<HashMap<String, String>> mArrayList;

        @Override
        protected Void doInBackground(String... params) {
            String input = params[0];
            String urlPrefix = "http://query.yahooapis.com/v1/public/yql?q=select%20symbol%2CChange%2C%20LastTradePriceOnly%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(";
            String urlSuffix = ")&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
            String url = urlPrefix + input + urlSuffix;
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
            refreshDisplay();
        }
    }
}




