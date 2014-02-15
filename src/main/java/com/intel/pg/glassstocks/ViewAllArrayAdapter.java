package com.intel.pg.glassstocks;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pgandhi on 2/10/14.
 */
public class ViewAllArrayAdapter extends BaseAdapter{
    private List<Stocks> mData;
    private Activity mContext;
    private LayoutInflater mLayoutInflater = null;

    public ViewAllArrayAdapter(Activity context, List<Stocks> list) {
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
            row = li.inflate(R.layout.view_all_row, null);
            holder = new Holder();
            holder.txtSymbol = (TextView)row.findViewById(R.id.view_all_row_symbol);
            holder.txtValue = (TextView)row.findViewById(R.id.view_all_row_value);
            holder.txtChange = (TextView)row.findViewById(R.id.view_all_row_change);
            row.setTag(holder);
        }
        else
        {
            holder = (Holder)row.getTag();
        }

        Stocks s = mData.get(position);
        holder.txtSymbol.setText(s.getSymbol());
        holder.txtValue.setText(s.getLastTradePrice());
        holder.txtChange.setText(s.getLastTradeChange());
        if (s.getLastTradeChange() != null) {
            if (s.getLastTradeChange().startsWith("+")) {
                holder.txtChange.setBackgroundColor(mContext.getResources().getColor(R.color.green));
            }
            else {
                holder.txtChange.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            }
        }
        holder.txtChange.invalidate();
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
        TextView txtSymbol;
        TextView txtValue;
        TextView txtChange;
    }
}
