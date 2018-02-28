package com.tcl.choujiang.choujiang;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcl.choujiang.choujiang.util.DBhelper;
import com.tcl.choujiang.choujiang.util.DataBaseManager;

import java.util.ArrayList;
import java.util.List;


public class AllLuckyManActivity extends AppCompatActivity {

    RecyclerView luckManRecycler;
    List<String> luckList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_lucky_man);
        initData();
        initRecyclerView();
    }

    public void initData() {
        luckList = new ArrayList<String>();
        initLuckMandata();
    }

    public void initLuckMandata() {
        luckList.clear();
        Cursor cursor = null;
        try {
            cursor = DataBaseManager.getInstance(AllLuckyManActivity.this).queryData(DBhelper.TABLE_REMOVENAMEINFO, null
                    , null, null, null, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor.isFirst()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String prizelevel = cursor.getString(cursor.getColumnIndex("prizelevel"));
            Log.e("iii", ":" + prizelevel);
            luckList.add(name.toString().substring(0, name.indexOf(".")) + " 获得  " + prizelevel);
        }
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String prizelevel = cursor.getString(cursor.getColumnIndex("prizelevel"));
            luckList.add(name.toString().substring(0, name.indexOf(".")) + " 获得  " + prizelevel);
        }
        Log.e("iii", "luckList size " + luckList.size());
    }

    public void initRecyclerView() {
        luckManRecycler = (RecyclerView) findViewById(R.id.allLuckMan);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        luckManRecycler.setLayoutManager(layoutManager);
        LuckyManAdapter luckyManAdapter = new LuckyManAdapter();
        luckManRecycler.setAdapter(luckyManAdapter);

    }

    class LuckyManAdapter extends RecyclerView.Adapter<LuckyManAdapter.myViewHolder> {

        @Override
        public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            myViewHolder viewHolder = new myViewHolder(LayoutInflater.from(AllLuckyManActivity.this)
                    .inflate(R.layout.nameshow_item, parent, false));

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(myViewHolder holder, int position) {
            holder.zjman.setText((position + 1) + " : " + luckList.get(position));
        }

        @Override
        public int getItemCount() {
            return luckList.size();
        }

        class myViewHolder extends RecyclerView.ViewHolder {

            TextView zjman;

            public myViewHolder(View itemView) {
                super(itemView);
                zjman = itemView.findViewById(R.id.nameSS);
            }
        }
    }


}
