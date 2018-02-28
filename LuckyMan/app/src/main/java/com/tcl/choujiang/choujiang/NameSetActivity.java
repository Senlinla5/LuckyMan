package com.tcl.choujiang.choujiang;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.choujiang.choujiang.util.DBhelper;
import com.tcl.choujiang.choujiang.util.DataBaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NameSetActivity extends AppCompatActivity {

    private Button fanhuiBtn;
    private Button daoruBtn;
    private Button piliangdaoruBtn;
    private RecyclerView mrecyclerView;
    private List<String> nameList;
    private TextView nameNum;
    private namseAdapter adapter;
    private List<String> allNameList = new ArrayList<String>();
    private Handler mhandler;
    private static final int MSG_UPDATE_NAMEINFO = 0x1100;
    private static final int MSG_UPDATE_ONENAME = 0x1101;
    private MyTask mTask;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_set);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarLarge);
        initHandler();
        findView();
        initData();
        initRecyclerView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mhandler.removeMessages(MSG_UPDATE_NAMEINFO);
        mhandler.removeMessages(MSG_UPDATE_ONENAME);
    }

    public void initHandler() {
        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_NAMEINFO:
                        adapter.notifyDataSetChanged();
                        //Utils.insertAllDataBase(NameSetActivity.this, nameList);
                        insertAllDataBase(NameSetActivity.this, nameList);
                        adapter.showNameNum();
                        break;
                    case MSG_UPDATE_ONENAME:
                        adapter.notifyDataSetChanged();
                        adapter.showNameNum();
                        insertOneDataBase();

                        break;
                }
            }
        };
    }

    public void initData() {
        nameList = new ArrayList<String>();
        //同步数据库的内容
        tongBuShuJuKu();

    }

    public void tongBuShuJuKu() {
        try {
            Cursor cursor = DataBaseManager.getInstance(this).queryData(
                    DBhelper.TABLE_NAMEINFO, null, null, null, null, null, null);
//            if(!cursor.moveToNext()){
//                nameList.add("sss");
//            }
            if (cursor != null) {
                Log.d("karer", "NameSet nameList cursor count " + cursor.getCount());
                if (cursor.isFirst()) {
                    //记得加上数据库第一个条数据
                    nameList.add("" + cursor.getString(cursor.getColumnIndex("name")));
                }
                while (cursor.moveToNext()) {
                    nameList.add("" + cursor.getString(cursor.getColumnIndex("name")));
                }
                Log.d("karer", "tongBuShuJuKu - namelist size: " + nameList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("karer", "namelist size " + e);
        }
        Log.d("karer", "NameSet nameList: " + nameList.size());
    }

    public void findView() {
        fanhuiBtn = (Button) findViewById(R.id.fanhui);
        daoruBtn = (Button) findViewById(R.id.daoru);
        piliangdaoruBtn = (Button) findViewById(R.id.piliangdaoru);
        nameNum = (TextView) findViewById(R.id.namenumber);
        fanhuiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("notifyname", "2");
                int resultcode = 100;
                NameSetActivity.this.setResult(resultcode, intent);
                NameSetActivity.this.finish();
            }
        });

        daoruBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(NameSetActivity.this);
                new AlertDialog.Builder(NameSetActivity.this).setTitle("输入文件全名")
                        .setView(editText).setPositiveButton("确定"
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!(editText.getText().toString().equals(""))) {
                                    isExistfileData(editText.getText().toString().trim());
                                }

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();

            }
        });

        piliangdaoruBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(NameSetActivity.this).setTitle("提示")
                        .setMessage("所有数据将被重置，你希望这样做吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //重置数据
                        nameList.clear();
                        //getAllFiles(new File(MainActivity.SDPATH));
                        //新建一个线程来处理导入时间
                        mTask = new MyTask();
                        mTask.execute("");

                        mhandler.sendEmptyMessage(MSG_UPDATE_NAMEINFO);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();


            }
        });
    }

    /**
     * 将所有数据存入数据库
     */
    public void insertAllDataBase(Context context, List<String> nameList) {
        try {
            DataBaseManager.getInstance(context).clearAllData(DBhelper.TABLE_NAMEINFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("karer", "insertAllDataBase - namelist size: " + nameList.size());
        for (int i = 0; i < nameList.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("uid", i);
            contentValues.put("name", nameList.get(i));
            try {
                long k = DataBaseManager.getInstance(context).insetData(DBhelper.TABLE_NAMEINFO, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DataBaseManager.getInstance(context).close();
    }

    /**
     * 插入一条数据
     */
    public void insertOneDataBase() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uid", nameList.size());
        contentValues.put("name", nameList.get(nameList.size() - 1));
        try {
            long n = DataBaseManager.getInstance(this).insetData(DBhelper.TABLE_NAMEINFO, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataBaseManager.getInstance(this).close();
    }

    public void initRecyclerView() {
        mrecyclerView = (RecyclerView) findViewById(R.id.nameshow);
        mrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerView.addItemDecoration(new MyItemDecoration());
        adapter = new namseAdapter();
        mrecyclerView.setAdapter(adapter);
        adapter.showNameNum();

    }

    class MyItemDecoration extends RecyclerView.ItemDecoration {
        /**
         * @param outRect 边界
         * @param view    recyclerView ItemView
         * @param parent  recyclerView
         * @param state   recycler 内部数据管理
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //设定底部边距为1px
            outRect.set(0, 0, 0, 1);
        }
    }


    /**
     * 获取文件夹中所有文件
     *
     * @param root
     */
    private void getAllFiles(File root) {
        Log.d("karer", " getAllFiles ");
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    Log.d("karer", " getAllFiles  isDirectory");
                    getAllFiles(f);
                } else {
                    if (f.getName().indexOf(".JPG") > 0 || f.getName().indexOf(".jpg") > 0 || f.getName().indexOf(".png") > 0) {
                        nameList.add(f.getName());
                        Log.d("iii", " path " + f.getPath());
                    }
                }
            }
            Log.d("karer", "before disruptOrder nameList: " + nameList.size());
            nameList = Utils.disruptOrder(nameList);
            Log.d("karer", "after disruptOrder nameList: " + nameList.size());
        }
    }

    /**
     * 导入单个数据
     *
     * @param filsStr
     */

    public void isExistfileData(String filsStr) {
        File root = new File("/sdcard/1");
        File files[] = root.listFiles();
        boolean isexit = false;
        boolean isExitNameList = false;
        for (int i = 0; i < nameList.size(); i++) {
            if (filsStr.equals(nameList.get(i))) {
                isExitNameList = true;
            }
        }
        if (files != null && !isExitNameList) {
            for (File f : files) {
                if (f.isDirectory()) {
                    getAllFiles(f);
                } else {
                    if (f.getName().indexOf(".JPG") > 0 || f.getName().indexOf(".jpg") > 0 || f.getName().indexOf(".png") > 0) {
                        String filenames = f.getName();
                        if (filsStr.equals(filenames)) {
                            isexit = true;
                            nameList.add(f.getName());

                            mhandler.sendEmptyMessage(MSG_UPDATE_ONENAME);
                        }

                    }
                }
            }
        }

        if (isexit) {
            Toast.makeText(NameSetActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(NameSetActivity.this, "导入失败", Toast.LENGTH_SHORT).show();
        }
    }

    class namseAdapter extends RecyclerView.Adapter<namseAdapter.myViewHolder> {


        @Override
        public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            myViewHolder viewholder = new myViewHolder(LayoutInflater.from(NameSetActivity.this)
                    .inflate(R.layout.nameset_item, parent, false));
            return viewholder;
        }

        @Override
        public void onBindViewHolder(myViewHolder holder, int position) {
            holder.nameText.setText(" " + (position + 1) + " ：" + nameList.get(position));
        }

        @Override
        public int getItemCount() {
            return nameList.size();
        }

        public void showNameNum() {
            nameNum.setText("  共 " + nameList.size() + " 人");
        }

        class myViewHolder extends ViewHolder {
            TextView nameText;

            public myViewHolder(View itemView) {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.namesetshow);
            }
        }
    }


    private class MyTask extends AsyncTask<String, Integer, String> {
        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            getAllFiles(new File(MainActivity.SDPATH));
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(NameSetActivity.this, "导入完成", Toast.LENGTH_SHORT).show();
            Log.d("karer", "MyTask - onPostExecute - namelist size: " + nameList.size());
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
        }
    }
}
