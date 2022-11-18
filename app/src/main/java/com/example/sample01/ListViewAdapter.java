package com.example.sample01;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class ListViewAdapter extends BaseAdapter {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<NoSmokingData> sample;

    public ListViewAdapter(Context context, ArrayList<NoSmokingData> data){
        mContext = context;
        sample = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }



    @Override
    public int getCount() {
        return sample.size();
    }

    @Override
    public NoSmokingData getItem(int i) {
        return sample.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View converview, ViewGroup viewGroup) {
        View view = mLayoutInflater.inflate(R.layout.item_list,null);

        TextView Name = (TextView) view.findViewById(R.id.nosmokingName);
        TextView Address = (TextView)view.findViewById(R.id.nosmokingAddress);

        Name.setText(sample.get(i).getName());
        Address.setText(sample.get(i).getAddress());


        return view;
    }
    public void addItemToList(String name, String address,double X,double Y){
        NoSmokingData noSmokingData = new NoSmokingData();

        noSmokingData.NoSmokingData(name,address,X,Y);

        sample.add(noSmokingData);


    }
}
