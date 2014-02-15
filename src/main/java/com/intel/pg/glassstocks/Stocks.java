package com.intel.pg.glassstocks;

import java.util.ArrayList;

/**
 * Created by pgandhi on 2/8/14.
 */
public class Stocks {
    int mID;
    String mSymbol;
    String mName;
    String mExchange;
    int mStarred;
    String mLastTradePrice;
    String mLastTradeChange;

    public Stocks() {

    }

    public Stocks(int id, String symbol, String name, String exchange, String value, String change, int starred) {
        this.mID = id;
        this.mSymbol = symbol;
        this.mName = name;
        this.mExchange = exchange;
        this.mLastTradePrice = value;
        this.mLastTradeChange = change;
        this.mStarred = starred;
    }
    public Stocks(int id, String symbol, String name, String exchange, int starred) {
        this.mID = id;
        this.mSymbol = symbol;
        this.mName = name;
        this.mExchange = exchange;
        this.mStarred = starred;
    }

    public Stocks(String symbol, String name, String exchange, int starred) {
        this.mSymbol = symbol;
        this.mName = name;
        this.mExchange = exchange;
        this.mStarred = starred;
    }

    public int getID() {
        return this.mID;
    }

    public void setID(int id) {
        this.mID = id;
    }

    public String getSymbol() {
        return this.mSymbol;
    }

    public void setSymbol(String symbol) {
        this.mSymbol = symbol;
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getExchange() {
        return this.mExchange;
    }

    public void setExchange(String exchange) {
        this.mExchange = exchange;
    }

    public int isStarred() {
        return this.mStarred;
    }

    public void setStarred(int starred) {
        this.mStarred = starred;
    }

    public String getLastTradePrice() {
        return this.mLastTradePrice;
    }

    public void setLastTradePrice(String price) {
        this.mLastTradePrice = price;
    }

    public String getLastTradeChange() {
        return this.mLastTradeChange;
    }

    public void setLastTradeChange(String change) {
        this.mLastTradeChange = change;
    }

    public ArrayList<String> getLastKnownData() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(getLastTradePrice());
        list.add(getLastTradeChange());
        return list;
    }

    public void setLastKnownData(ArrayList<String> list) {
        if (list.size() < 2) {
            throw new IndexOutOfBoundsException();
        }
        setLastTradePrice(list.get(0));
        setLastTradeChange(list.get(1));
    }

}
