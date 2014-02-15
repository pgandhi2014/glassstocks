package com.intel.pg.glassstocks;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pgandhi on 2/5/14.
 */

public class PortfolioArrayAdapter extends BaseAdapter {
    private List<Stocks> mData;
    private Activity mContext;
    private LayoutInflater mLayoutInflater = null;

    public PortfolioArrayAdapter(Activity context, List<Stocks> list) {
        super();
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = list;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Stocks getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = null;

        if(row == null)
        {
            LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = li.inflate(R.layout.portfolio_row, null);
            holder = new Holder();
            holder.txtTitle = (TextView)row.findViewById(R.id.portfolio_row_symbol);
            holder.imgStarred = (ImageView)row.findViewById(R.id.portfolio_row_starred);
            row.setTag(holder);
        }
        else
        {
            holder = (Holder)row.getTag();
        }

        Stocks s = mData.get(position);
        holder.txtTitle.setText(s.getSymbol() + " (" + s.getName() + ")");

        if (s.isStarred() == 1){
            holder.imgStarred.setImageResource(R.drawable.ic_star);
        }
        else {
            holder.imgStarred.setImageResource(0);
        }
        return row;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void refreshAdapter(List<Stocks> list) {
        mData = list;
        notifyDataSetChanged();
    }

    static class Holder
    {
        TextView txtTitle;
        ImageView imgStarred;
    }
}
