package com.intel.pg.glassstocks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pgandhi on 2/2/14.
 */

public class SymbolLookupActivity extends Activity {
    private static final String TAG = "SymbolLookup";
    private ArrayList<Card> mCards;
    private CardScrollView mCardScrollView;
    private ArrayList<HashMap<String, String>> mArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArrayList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("arraylist");

        createCards();

        mCardScrollView = new CardScrollView(this);
        StocksCardScrollAdapter adapter = new StocksCardScrollAdapter();
        mCardScrollView.setAdapter(adapter);
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", i);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
        mCardScrollView.activate();
        setContentView(mCardScrollView);
    }


    private void createCards() {
        mCards = new ArrayList<Card>();

        Card card;
        for (HashMap<String,String> map : mArrayList) {
            card = new Card(this);
            card.setText(map.get("Name") + " (" + map.get("Symbol") + ")");
            card.setFootnote(map.get("Exchange"));
            mCards.add(card);
        }
    }

    private class StocksCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int findIdPosition(Object id) {
            return -1;
        }

        @Override
        public int findItemPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).toView();
        }
    }
}
