package com.tcl.choujiang.choujiang;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.choujiang.choujiang.util.DBhelper;
import com.tcl.choujiang.choujiang.util.DataBaseManager;

import java.util.ArrayList;
import java.util.List;


public class PrizeActivity extends AppCompatActivity {

    private RecyclerView mRecyclerview;
    //    private List<String> prizeList;
    private List<JiangXiang> jiangXiangList;
    private prizeAdapter prizeadapter;
    private Handler mhandler;
    public static final int MSG_UPDATA_ALLPRICE = 0x2200;
    public static final int MSG_UPDATA_ONEPRICE = 0x2201;
    public boolean finish = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prize);
        initHandler();
        initData();
        initRecycleView();
        findView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mhandler.removeMessages(MSG_UPDATA_ONEPRICE);

    }

    public void initHandler() {
        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATA_ONEPRICE:
                        prizeadapter.notifyDataSetChanged();
                        break;
                    case MSG_UPDATA_ALLPRICE:
                        break;
                }
            }
        };
    }

    public void findView() {
        //prizeSaveBtn = (Button) findViewById(R.id.prizeSave);
//        addPrizeBtn = (TextView) findViewById(R.id.addPrize);
//        prizeSaveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //点击返回的时候，保存数据
//                savePrizeData();
//                Intent intent = new Intent();
//                intent.putExtra("notify", "1");
//                int resultcode = 0;
//                PrizeActivity.this.setResult(resultcode, intent);
//                PrizeActivity.this.finish();
//            }
//        });

//        addPrizeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                prizeadapter.addData(prizeList.size());
//
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        savePrizeData();
        if (!finish) return;

        Intent intent = new Intent();
        intent.putExtra("notify", "1");
        int resultcode = 0;
        PrizeActivity.this.setResult(resultcode, intent);
        PrizeActivity.this.finish();
        super.onBackPressed();
    }

    /**
     * 返回前保存数据
     */
    public void savePrizeData() {
        try {
            DataBaseManager.getInstance(PrizeActivity.this).clearAllData(DBhelper.TABLE_PRIZEINFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jiangXiangList.size(); i++) {
            String jx = jiangXiangList.get(i).getJiangXiang();
            String count = jiangXiangList.get(i).getCount();

            finish = true;
            if (TextUtils.isEmpty(jx) || TextUtils.isEmpty(count)) {
                Toast.makeText(this, "奖项设置有空，请重新设置", Toast.LENGTH_LONG).show();
                finish = false;
                return;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put("uid", i);
            contentValues.put("prizename", jx);
            contentValues.put("count", count);
            contentValues.put("last", PrizeActivity.this.queryLastNumber(jx));

            try {
                DataBaseManager.getInstance(PrizeActivity.this).insetData(DBhelper.TABLE_PRIZEINFO, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DataBaseManager.getInstance(PrizeActivity.this).close();
    }

    public void initData() {
//        prizeList = new ArrayList<String>();
//        prizeList.clear();
        //karer
        jiangXiangList = new ArrayList<JiangXiang>();
        jiangXiangList.clear();

//        for(int i=0;i<1;i++){
//            prizeList.add("一等奖：iPhone X");
//        }
        Cursor cursor = null;
        try {
            cursor = DataBaseManager.getInstance(PrizeActivity.this).queryData(DBhelper.TABLE_PRIZEINFO, null, null
                    , null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor.isFirst()) {
            String prizeName = cursor.getString(cursor.getColumnIndex("prizename"));
            String prizeCount = cursor.getString(cursor.getColumnIndex("count"));//karer
            int uid = cursor.getInt(cursor.getColumnIndex("uid"));
            Log.e("iii", " uid " + uid + " prize name " + prizeName);
//            prizeList.add(uid, prizeName);
            JiangXiang jx = new JiangXiang(prizeName, prizeCount, prizeCount);
            jiangXiangList.add(uid, jx);
        }
        while (cursor.moveToNext()) {
            String prizeName = cursor.getString(cursor.getColumnIndex("prizename"));
            String prizeCount = cursor.getString(cursor.getColumnIndex("count"));//karer
            int uid = cursor.getInt(cursor.getColumnIndex("uid"));
//            prizeList.add(uid, prizeName);
            JiangXiang jx = new JiangXiang(prizeName, prizeCount, prizeCount);
            jiangXiangList.add(uid, jx);
        }
        DataBaseManager.getInstance(PrizeActivity.this).close();
    }

    public void initRecycleView() {
        mRecyclerview = (RecyclerView) findViewById(R.id.id_recyclerview);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        prizeadapter = new prizeAdapter();
        mRecyclerview.setAdapter(prizeadapter);
    }

    class prizeAdapter extends RecyclerView.Adapter<prizeAdapter.myViewHolder> {

        @Override
        public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            myViewHolder holder = new myViewHolder(LayoutInflater.from(PrizeActivity.this).inflate(R.layout.prize_item
                    , parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(final myViewHolder holder, final int position) {
            //holder.prizeEdit.setText(prizeList.get(position));
            holder.prizeEdit.setText(jiangXiangList.get(position).getJiangXiang());
            holder.prizeCountEdit.setText(jiangXiangList.get(position).getCount());
            holder.prizeRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (prizeList.size() == 1) {
//
//                    } else {
//                        removeData(position);
//                    }

                    if (jiangXiangList.size() == 1) {

                    } else {
                        removeData(position);
                    }
                }
            });

            holder.prizeAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("karer", " holder.prizeAdd");
                    addData(jiangXiangList.size());
                }
            });

            holder.prizeEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.e("iiii", " beforeTextChanged");
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.e("iiii", " onTextChanged");
                }

                @Override
                public void afterTextChanged(Editable editable) {
//                    prizeList.remove(position);
//                    prizeList.add(position, holder.prizeEdit.getText().toString());
//                    jiangXiangList.remove(position);
//                    JiangXiang jx = new JiangXiang(holder.prizeEdit.getText().toString(), holder.prizeCountEdit.getText().toString(), "");
//                    jiangXiangList.add(position, jx);
                    // Log.e("iiii", "position" + position + " afterTextChanged" + prizeList.get(position));
                    updateEdit(position, holder);
                }
            });

            holder.prizeCountEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
//                    prizeList.remove(position);
//                    prizeList.add(position, holder.prizeEdit.getText().toString());
//                    jiangXiangList.remove(position);
//                    JiangXiang jx = new JiangXiang(holder.prizeEdit.getText().toString(), holder.prizeCountEdit.getText().toString());
//                    jiangXiangList.add(position, jx);
//                    Log.e("iiii", "position" + position + " afterTextChanged" + jiangXiangList.get(position));
                    updateEdit(position, holder);
                }
            });

        }

        private void updateEdit(int position, final myViewHolder holder) {
            jiangXiangList.remove(position);
            String prizeName = holder.prizeEdit.getText().toString();
            String prizeCount = holder.prizeCountEdit.getText().toString();
            JiangXiang jx = new JiangXiang(prizeName, prizeCount, queryLastNumber(prizeName));
            jiangXiangList.add(position, jx);
        }

        @Override
        public int getItemCount() {
            return jiangXiangList.size();
        }

        public void addData(int position) {
            Log.d("karer", "addData");
//            prizeList.add("");
            jiangXiangList.add(new JiangXiang("", "", ""));
            notifyItemInserted(position);
        }

        public void removeData(int position) {
            //prizeList.remove(position);
            jiangXiangList.remove(position);
            notifyItemRemoved(position);
            notifyDataSetChanged();
        }

        class myViewHolder extends ViewHolder {
            EditText prizeEdit;
            EditText prizeCountEdit;
            TextView prizeAdd;
            TextView prizeRemove;

            public myViewHolder(View itemView) {
                super(itemView);
                prizeEdit = (EditText) itemView.findViewById(R.id.prizeEdit);
                prizeCountEdit = (EditText) itemView.findViewById(R.id.prize_count);
                prizeAdd = (TextView) itemView.findViewById(R.id.prizeAdd);
                prizeRemove = (TextView) itemView.findViewById(R.id.prizeRemove);
            }
        }
    }

    public String queryLastNumber(String prizeName) {
        Cursor cursor = null;
        String selection = "prizename=?";
        String[] selecttionArgs = new String[]{prizeName};
        String last = "";
        try {
            cursor = DataBaseManager.getInstance(PrizeActivity.this).queryData(DBhelper.TABLE_PRIZEINFO, null, selection
                    , selecttionArgs, null, null, null, null);
            Log.d("karer", "cursor : " + cursor);
            if (cursor != null) {
                Log.d("karer", "cursor size: " + cursor.getCount());
            }
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                last = cursor.getString(cursor.getColumnIndex("last"));
                Log.d("karer", "last : " + last);
                return last != null ? last : "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataBaseManager.getInstance(PrizeActivity.this).close();
        }
        return last;
    }
}
