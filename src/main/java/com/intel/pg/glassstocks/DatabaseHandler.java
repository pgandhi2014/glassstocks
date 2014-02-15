package com.intel.pg.glassstocks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pgandhi on 2/8/14.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StocksDatabase";
    private static final String TABLE_NAME = "stocks";

    // Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_SYMBOL = "symbol";
    private static final String KEY_NAME = "name";
    private static final String KEY_EXCHANGE = "exchange";
    private static final String KEY_LASTVALUE = "lastvalue";
    private static final String KEY_LASTCHANGE = "lastchange";
    private static final String KEY_STARRED = "starred";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SYMBOL + " TEXT UNIQUE,"
                + KEY_NAME + " TEXT," + KEY_EXCHANGE + " TEXT,"
                + KEY_LASTVALUE + " TEXT," + KEY_LASTCHANGE + " TEXT,"
                + KEY_STARRED + " INTEGER" +")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    int addStock(Stocks stock) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SYMBOL, stock.getSymbol());
        values.put(KEY_NAME, stock.getName());
        values.put(KEY_EXCHANGE, stock.getExchange());
        values.put(KEY_LASTVALUE, stock.getLastTradePrice());
        values.put(KEY_LASTCHANGE, stock.getLastTradeChange());
        values.put(KEY_STARRED, stock.isStarred());

        int row_id = (int)db.insert(TABLE_NAME, null, values);
        db.close();
        return row_id;
    }

    Stocks getStock(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_SYMBOL,
                KEY_NAME, KEY_EXCHANGE, KEY_LASTVALUE, KEY_LASTCHANGE,
                KEY_STARRED }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Stocks stock = new Stocks(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), Integer.parseInt(cursor.getString(6)));
        cursor.close();
        db.close();
        return stock;
    }

    public List<Stocks> getAllStocks() {
        List<Stocks> stockList = new ArrayList<Stocks>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Stocks stock = new Stocks();
                stock.setID(Integer.parseInt(cursor.getString(0)));
                stock.setSymbol(cursor.getString(1));
                stock.setName(cursor.getString(2));
                stock.setExchange(cursor.getString(3));
                stock.setLastTradePrice(cursor.getString(4));
                stock.setLastTradeChange(cursor.getString(5));
                stock.setStarred(Integer.parseInt(cursor.getString(6)));

                stockList.add(stock);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return stockList;
    }

    public List<Stocks> getStarredStocks() {
        List<Stocks> stockList = new ArrayList<Stocks>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_ID, KEY_SYMBOL,
                KEY_NAME, KEY_EXCHANGE, KEY_LASTVALUE, KEY_LASTCHANGE,
                KEY_STARRED}, KEY_STARRED + " = 1", null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Stocks stock = new Stocks();
                stock.setID(Integer.parseInt(cursor.getString(0)));
                stock.setSymbol(cursor.getString(1));
                stock.setName(cursor.getString(2));
                stock.setExchange(cursor.getString(3));
                stock.setLastTradePrice(cursor.getString(4));
                stock.setLastTradeChange(cursor.getString(5));
                stock.setStarred(Integer.parseInt(cursor.getString(6)));

                stockList.add(stock);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stockList;
    }
      public int updateStarredStatus(int id, int starred) {
          SQLiteDatabase db = this.getWritableDatabase();
          int retVal = 0;
          ContentValues values = new ContentValues();
          values.put(KEY_STARRED, starred);

          retVal = db.update(TABLE_NAME, values, KEY_ID + " = ?", new String[] { String.valueOf(id) });
          db.close();
          return retVal;
    }

    public int updateLastTrade(int id, String value, String change) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retVal = 0;
        ContentValues values = new ContentValues();
        values.put(KEY_LASTVALUE, value);
        values.put(KEY_LASTCHANGE, change);

        retVal = db.update(TABLE_NAME, values, KEY_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
        return retVal;
    }

    public void deleteStock(Stocks stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?", new String[] { String.valueOf(stock.getID()) });
        db.close();
    }

    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int getStarCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_STARRED + "=1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
