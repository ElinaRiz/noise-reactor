package com.example.noisereactor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class MyCustomAdapter extends BaseAdapter {

    private ArrayList<Cell> mData = new ArrayList<Cell>();
    private LayoutInflater mInflater;
    private Context mContext;

    public MyCustomAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    public void addItem(final Cell item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Cell getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.cell, null);
        }

        Cell p = mData.get(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.cell_data);
            TextView tt2 = (TextView) v.findViewById(R.id.cell_value);
            TextView tt3 = (TextView) v.findViewById(R.id.cell_norm);

            if (tt1 != null) {
                tt1.setText(p.data);
            }

            if (tt2 != null) {
                tt2.setText(p.noise);
            }

            if (tt3 != null) {
                tt3.setText(p.norm);
            }
        }

        return v;
    }

}
