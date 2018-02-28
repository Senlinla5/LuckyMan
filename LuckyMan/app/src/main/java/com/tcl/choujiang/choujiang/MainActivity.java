package com.tcl.choujiang.choujiang;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.tcl.choujiang.choujiang.util.DBhelper;
import com.tcl.choujiang.choujiang.util.DataBaseManager;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends AppCompatActivity {
    private static final boolean DBG = true;
    ImageView imageView;
    ImageButton mPrizeSet;
    ImageButton mNameSet;
    ImageButton realStart;
    TextView zjName;
    private HandlerThread mCheckMsgThread;
    private Handler mCheckMsgHandler;
    private static final int MSG_UPDATE_INFO = 0x110;
    private static final int MSG_UPDATA_BANGDAN = 0x130;
    private static final int MSG_DELAY_STOP = 0x120;
    private static final int MSG_STOP = 0x120;
    private static int DELAY_MILLINS = 0;
    private Boolean mStopState = false;
    private Boolean mDelayStopState = false;
    private ArrayList<File> list = new ArrayList<File>();
    /**
     * save the raw data list
     */
    public static ArrayList<File> rawData = new ArrayList<File>();
    /**
     * temp data list: removed zhongjiang lists
     */
    public static ArrayList<File> tempData = new ArrayList<File>();
    private ArrayList<String> mNameList = new ArrayList<String>();
    private ArrayList<File> zhongjiangList = new ArrayList<File>();
    private List<String> allluckyman;
    public static final int prizeRequestCode = 1;
    public static final int nameRequestCode = 2;
    public static final int allLuckyManRequestCode = 3;
    public RecyclerView mBangdanRecycler;
    private List<String> namelist;
    private List<String> prizelist;
    private List<String> ZJlist;
    private List<String> ZJNameList;//中奖人名单列表
    private bangdanAdapter bangAdapter;
    private Spinner spinner;
    //文件位置
    public static final String SDPATH = "/sdcard/choujiang/1/";
    public String ZJNAME = "";
    public String JDJIANG = "";
    public int MPOSITION = 0;
    ImageButton resetLayout;
    private ArrayAdapter<String> spinnerAdapter;
    ImageButton fangQiZJ;
    ImageButton zongBd;
    ImageButton mLeftButton;
    ImageButton mRightButton;
    ImageButton mShare;
    TextView mNotice;

    Bitmap mShareResult;

    TextView bDTitle;
    TextView JDjiang;
    boolean flag;//第一次进oncreate，防止在onresume中再一次清楚榜单列表
    int prizeFlag;//记录几等奖的关键字
    int index;
    private SharedPreferences sp;

    private boolean choujiangOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        sp = getSharedPreferences("CHOUJIANG", MODE_PRIVATE);
        flag = true;
        prizeFlag = 0;
        index = 0;
        initBackThread();
        imageView = (ImageView) findViewById(R.id.image_view);
        mPrizeSet = (ImageButton) findViewById(R.id.prizeset);
        mNameSet = (ImageButton) findViewById(R.id.nameset);
        realStart = (ImageButton) findViewById(R.id.main_real_start);
        zjName = (TextView) findViewById(R.id.nameZJ);
        resetLayout = (ImageButton) findViewById(R.id.reset_choujiang);
        fangQiZJ = (ImageButton) findViewById(R.id.fangQiZJ);
        zongBd = (ImageButton) findViewById(R.id.zongBangDan);
        bDTitle = (TextView) findViewById(R.id.BDZJ);
        JDjiang = (TextView) findViewById(R.id.current_jiang);
        mNotice = (TextView) findViewById(R.id.notice);

        mShare = (ImageButton) findViewById(R.id.bt_share);

        //left/right button
        mLeftButton = (ImageButton) findViewById(R.id.bt_left);
        mRightButton = (ImageButton) findViewById(R.id.bt_right);

        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: need get the real zhongjiang List
                imageView.setImageBitmap(mShareResult);
            }
        });

        fangQiZJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this).setTitle("提示").setMessage("确定放弃奖项吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //放弃奖项的时候，把人从中奖列表中删除，并且添加到所有人的名单中
                                if (!ZJNAME.equals("") && ZJNameList.size() > 0) {
                                    giveUpPrize();
                                } else {
                                    Toast.makeText(MainActivity.this, "没有中奖信息", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
            }
        });
        zongBd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AllLuckyManActivity.class);
                startActivity(intent);
            }
        });
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: need get the real zhongjiang List
                //String [] toBeStored = new String[allluckyman.size()];
                //allluckyman.toArray(toBeStored);

                test();

                /*
                initLuckData();
                StringBuilder sb = new StringBuilder();
                for (String s : allluckyman) {
                    sb.append(s).append("\n");
                }
                Log.e("sb：  ", sb.toString());
                mShareResult = QRCodeEncoder.syncEncodeQRCode(sb.toString(), 400);
                zjName.setVisibility(View.GONE);
                imageView.setImageBitmap(mShareResult);
                */
            }
        });

        resetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this).setTitle("提示").setMessage("确定重置吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //抽奖重置，把移除数据库中的数据 重新加载到 nameinfo的数据库
                                resetData();
                                initNameData();
                                ZJlist.clear();
                                ZJNameList.clear();
                                bangAdapter.notifyDataSetChanged();
                                Toast.makeText(MainActivity.this, "抽奖名单已经重置", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
            }
        });
        realStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zjName.setVisibility(View.VISIBLE);
                if (choujiangOver) {
                    Toast.makeText(MainActivity.this, "此项奖已经抽完，不够尽兴？再去奖项设置里面加一个吧", Toast.LENGTH_LONG).show();
                    return;
                }

                if (namelist.size() > 0) {
                    mStopState = !mStopState;
                    //start
                    if (mStopState) {
                        realStart.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.ic_pause_action));
                        mDelayStopState = false;
                        //pause
                    } else {
//                        //显示中奖榜单
//                        String zhongJ = "恭喜！" + ZJNAME.toString().substring(0, ZJNAME.toString().indexOf(".")) + " 获得 " + JDJIANG;
//                        ZJlist.add(zhongJ);
//                        ZJNameList.add(ZJNAME);
//                        //把中奖的人添加到移除数据库中，
//                        updataDataBase();
//                        bangAdapter.notifyDataSetChanged();
//                        mBangdanRecycler.scrollToPosition(ZJNameList.size() - 1);
                        realStart.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.ic_start_action));
//                        Log.d("karer", zhongJ);
                        mCheckMsgHandler.sendEmptyMessageDelayed(MSG_DELAY_STOP, 100);
                    }
                    mCheckMsgHandler.sendEmptyMessageDelayed(MSG_UPDATE_INFO, 0);
                } else {
                    Toast.makeText(MainActivity.this, "请先添加抽奖人员", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mPrizeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PrizeActivity.class);
                startActivityForResult(intent, prizeRequestCode);
            }
        });

        mNameSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NameSetActivity.class);
                startActivityForResult(intent, nameRequestCode);
            }
        });

        initData();
        initSpinner();
        initRecyclerView();
        initLuckData();
    }

    public void resetData() {
        List<String> tempList = new ArrayList<String>();
        tempList.clear();
        Cursor cursor = null;
        try {
            cursor = DataBaseManager.getInstance(MainActivity.this).queryData(DBhelper.TABLE_REMOVENAMEINFO, null
                    , null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor.isFirst()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            tempList.add(name);
        }
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            tempList.add(name);
        }
        try {
            //删除所有数据
            DataBaseManager.getInstance(MainActivity.this).clearAllData(DBhelper.TABLE_REMOVENAMEINFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cursor scursor = null;
        for (int i = 0; i < tempList.size(); i++) {
            //往nameinfo插入数据库
            ContentValues contentValues = new ContentValues();
            contentValues.put("uid", i);
            contentValues.put("name", tempList.get(i));
            try {
                DataBaseManager.getInstance(MainActivity.this).insetData(DBhelper.TABLE_NAMEINFO, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            DataBaseManager.getInstance(MainActivity.this).clearAllData(DBhelper.TABLE_LEFTNAMEINFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataBaseManager.getInstance(MainActivity.this).close();
        updateCurrentTimes(JDJIANG);
    }

    /**
     * 放弃中奖
     */
    public void giveUpPrize() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uid", 0);
        contentValues.put("name", ZJNAME);
        namelist.add(ZJNAME);
        int pos = ZJlist.size();
        int posi = ZJNameList.size();
        ZJlist.remove(pos - 1);
        ZJNameList.remove(posi - 1);
        bangAdapter.notifyDataSetChanged();
        try {
            DataBaseManager.getInstance(MainActivity.this).deleteData(DBhelper.TABLE_REMOVENAMEINFO
                    , "name=?", new String[]{ZJNAME});
            DataBaseManager.getInstance(MainActivity.this).insetData(DBhelper.TABLE_NAMEINFO, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataBaseManager.getInstance(MainActivity.this).close();

    }

    /**
     * 删除namelist中的数据
     * 删除TABLE_NAMEINFO表中的数据
     * 添加TABLE_REMOVENAMEINFO表中数据
     */
    public void updataDataBase() {
        Log.d("karer", "updataDataBase E");
        ContentValues contentValues = new ContentValues();
        contentValues.put("uid", 0);
        contentValues.put("name", ZJNAME);
        contentValues.put("prizelevel", JDJIANG);
        Log.d("karer", "updataDataBase namelist size MPOSITION:  " + MPOSITION + ", name: " + ZJNAME);
        try {
            namelist.remove(MPOSITION);
            Log.d("karer", "updataDataBase namelist size:  " + namelist.size());
            DataBaseManager.getInstance(MainActivity.this).deleteData(DBhelper.TABLE_NAMEINFO
                    , "name=?", new String[]{ZJNAME});
            DataBaseManager.getInstance(MainActivity.this).insetData(DBhelper.TABLE_REMOVENAMEINFO, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataBaseManager.getInstance(MainActivity.this).close();
        updateCurrentCount(JDJIANG);
        //mBackTask = new BackTask();
        //mBackTask.execute("");
    }

    /**
     * 根据奖项来查找更新剩余抽奖次数.剩余抽奖次数是奖项中的总数，减去已中奖表中的该奖项的人数
     *
     * @param jiangXiang 奖项
     */
    public void updateCurrentCount(String jiangXiang) {
//        //1. 拿出该奖项总数
//        int allCount = Integer.parseInt(Utils.queryCount(this, jiangXiang));
//        Log.d("kaer", "allCount: " + allCount);
//        //2. 拿出该奖项已中奖数目
//        int zhongjiangCount = Utils.queryNumber(this, jiangXiang);
//        Log.d("kaer", "zhongjiangCount: " + zhongjiangCount);
//        //3. 1 - 2 = 剩余次数
//
//        int lastTimes = allCount - zhongjiangCount;
//        if (lastTimes <= 0) {
//            Log.d("kaer", "lastTimes: " + lastTimes);
//        }
        int lastTimes = updateCurrentTimes(jiangXiang);

        ContentValues contentValues = new ContentValues();
        contentValues.put("last", lastTimes);//剩余次数
        String where = "prizename = ?";
        String[] whereArgs = new String[]{jiangXiang};
        try {
            DataBaseManager.getInstance(MainActivity.this).updateData(DBhelper.TABLE_PRIZEINFO, contentValues, where, whereArgs);
        } catch (Exception e) {
            Log.e("karer", "e: " + e.toString());
            e.printStackTrace();
        } finally {
            DataBaseManager.getInstance(MainActivity.this).close();
        }
    }

    public int updateCurrentTimes(String jiangXiang) {
//        String jiangXiang = "";
        //1. 拿出该奖项总数
        int allCount = Integer.parseInt(Utils.queryCount(this, jiangXiang));
        Log.d("kaer", "allCount: " + allCount);
        //2. 拿出该奖项已中奖数目
        int zhongjiangCount = Utils.queryNumber(this, jiangXiang);
        Log.d("kaer", "zhongjiangCount: " + zhongjiangCount);
        //3. 1 - 2 = 剩余次数

        int lastTimes = allCount - zhongjiangCount;
        String notice = "";
        if (lastTimes <= 0) {
            Log.d("kaer", "lastTimes: " + lastTimes);
            notice = allCount + "个奖已抽完";
            choujiangOver = true;
        } else {
            notice = "准备抽取第" + (zhongjiangCount + 1) + "个\n已抽取" + zhongjiangCount + "/" + allCount;
            choujiangOver = false;
        }
        mNotice.setText(notice);
        return lastTimes;
    }

    public String queryLastNumber(String prizeName) {
        Cursor cursor = null;
        String selection = "prizename=?";
        String[] selecttionArgs = new String[]{prizeName};
        String last = "";
        try {
            cursor = DataBaseManager.getInstance(this).queryData(DBhelper.TABLE_PRIZEINFO, null, selection
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
            DataBaseManager.getInstance(this).close();
        }
        return last;
    }

    public void initSpinner() {
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, prizelist);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinner.setAdapter(spinnerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("iiii", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
//senlin add
        bDTitle.setText(prizelist.get(index));
        Log.e("iiiiiii", "prizelist.size()" + prizelist.size());
        JDjiang.setText(prizelist.get(index));
        JDJIANG = prizelist.get(index);
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index < (prizelist.size() - 1)) {
                    index = index + 1;
                    JDjiang.setText(prizelist.get(index));
                    bDTitle.setText(prizelist.get(index));
                    JDJIANG = prizelist.get(index);
                    clearBangDan();
                    updateCurrentTimes(JDJIANG);
                } else {
                    //do nothing
                }
            }
        });
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index == 0) {
                    //do nothing
                } else {
                    index = index - 1;
                    JDjiang.setText(prizelist.get(index));
                    bDTitle.setText(prizelist.get(index));
                    JDJIANG = prizelist.get(index);
                    clearBangDan();
                    updateCurrentTimes(JDJIANG);
                }

            }
        });
        //senlin add
        Log.e("iiii", "onResume");
        spinnerAdapter.notifyDataSetChanged();
        prizeFlag = sp.getInt("prizeflag", 0);
        //必须加上这句话，否则回调走两次
        spinner.setSelection(prizeFlag, false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //获取当下抽取的是哪一个奖项
                prizeFlag = i;
                //JDJIANG = prizelist.get(i);
                //bDTitle.setText(JDJIANG);
                //选择不同奖项时候，把榜单数据清空
                clearBangDan();
                Log.e("iiii", "on- -- ----onItemSelected");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.e("iiii", "on- -- ----onNothingSelected");
            }
        });
        initBangDanInfo();
        updateCurrentTimes(JDJIANG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("iiii", "onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("iiii", "onStop");
        saveBangdan();
        sp.edit().putInt("prizeflag", prizeFlag).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("iiii", "onDestroy");

        mCheckMsgHandler.removeMessages(MSG_UPDATE_INFO);
    }


    public void clearBangDan() {
        ZJNameList.clear();
        bangAdapter.notifyDataSetChanged();
    }

    public void initData() {
        namelist = new ArrayList<String>();
        prizelist = new ArrayList<String>();
        ZJlist = new ArrayList<String>();
        ZJNameList = new ArrayList<String>();
        initPrizeData();
        initNameData();

    }

    public void initNameData() {
        namelist.clear();
        Cursor cursor = null;
        try {
            cursor = DataBaseManager.getInstance(this).queryData(DBhelper.TABLE_NAMEINFO, null, null, null, null
                    , null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor.isFirst()) {
            String sname = cursor.getString(cursor.getColumnIndex("name"));
            int uid = cursor.getInt(cursor.getColumnIndex("uid"));
            namelist.add(sname);
        }
        while (cursor.moveToNext()) {
            String sname = cursor.getString(cursor.getColumnIndex("name"));
            int uid = cursor.getInt(cursor.getColumnIndex("uid"));
            namelist.add(sname);
        }
        Log.d("kaer", "initNameData - namelist size: " + namelist.size());

    }

    public void initPrizeData() {
        prizelist.clear();
        Cursor cursor = null;
        try {
            cursor = DataBaseManager.getInstance(MainActivity.this).queryData(DBhelper.TABLE_PRIZEINFO, null, null
                    , null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!cursor.moveToFirst()) {
            prizelist.add("一等奖：iPhone X");
            bDTitle.setText("一等奖：iPhone X");
            //插入数据库
            ContentValues values = new ContentValues();
            values.put("uid", 0);
            values.put("prizename", prizelist.get(0));
            try {
                DataBaseManager.getInstance(MainActivity.this).insetData(DBhelper.TABLE_PRIZEINFO, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
            DataBaseManager.getInstance(MainActivity.this).close();
        }
        if (cursor.isFirst()) {
            String prizeName = cursor.getString(cursor.getColumnIndex("prizename"));
            int uid = cursor.getInt(cursor.getColumnIndex("uid"));
            prizelist.add(uid, prizeName);
        }
        while (cursor.moveToNext()) {
            String prizeName = cursor.getString(cursor.getColumnIndex("prizename"));
            int uid = cursor.getInt(cursor.getColumnIndex("uid"));
            prizelist.add(uid, prizeName);
        }
    }

    public void initRecyclerView() {
        mBangdanRecycler = (RecyclerView) findViewById(R.id.choujiangbangdan);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        //实现倒序排列
        layout.setStackFromEnd(true);//列表再底部开始展示，反转后由上面开始展示
        layout.setReverseLayout(true);//列表翻转
        layout.scrollToPositionWithOffset(0, 0);
        layout.setStackFromEnd(true);
        mBangdanRecycler.setLayoutManager(layout);
        bangAdapter = new bangdanAdapter();
        mBangdanRecycler.setAdapter(bangAdapter);

    }

    public void initLuckData() {
        allluckyman = new ArrayList<String>();
        initLuckMandata();
    }

    public void initLuckMandata() {
        allluckyman.clear();
        Cursor cursor = null;
        try {
            cursor = DataBaseManager.getInstance(MainActivity.this).queryData(DBhelper.TABLE_REMOVENAMEINFO, null
                    , null, null, null, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor.isFirst()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String prizelevel = cursor.getString(cursor.getColumnIndex("prizelevel"));
            Log.e("iii", ":" + prizelevel);
            allluckyman.add(name.toString().substring(0, name.indexOf(".")) + " " + prizelevel);
        }
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String prizelevel = cursor.getString(cursor.getColumnIndex("prizelevel"));
            allluckyman.add(name.toString().substring(0, name.indexOf(".")) + " " + prizelevel);
        }
        Log.e("iii", "luckList size " + allluckyman.size());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case prizeRequestCode:
                initPrizeData();
                break;
            case nameRequestCode:
                initNameData();
                break;
        }
    }

    int mIndex = 0;

    private void initBackThread() {
        mCheckMsgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_INFO:
                        //if (!mStopState) {
                        Log.d("karer", "恭喜: MSG_UPDATE_INFO mDelayStopState: " + mDelayStopState);
                        if (mDelayStopState) {
                            //显示中奖榜单
                            String zhongJ = "";
                            Log.d("karer", "ZJNAME  " + ZJNAME);
                            if (!TextUtils.isEmpty(ZJNAME)) {
                                zhongJ = "恭喜！" + ZJNAME.toString().substring(0, ZJNAME.toString().indexOf(".")) + " 获得 " + JDJIANG;
                            }
                            Log.d("karer", "ZJlist.contains(zhongJ):  " + ZJlist.contains(zhongJ));
                            if (ZJlist != null && !ZJlist.contains(zhongJ)) {
                                ZJlist.add(zhongJ);
                                ZJNameList.add(ZJNAME);
                                //把中奖的人添加到移除数据库中，
                                updataDataBase();
                                bangAdapter.notifyDataSetChanged();
                                mBangdanRecycler.scrollToPosition(ZJNameList.size() - 1);
                                Log.d("karer", zhongJ);
                            }
                            return;
                        }
//                        if (mIndex > namelist.size() - 1) {
//                            mIndex = 0;
//                        }

                        int index = Utils.randomNumber(namelist.size());//double randowm
                        String url = SDPATH + namelist.get(index);
                        File f = new File(url);
                        Glide.with(MainActivity.this)
                                //.load(resList[mIndex++])
                                .load(f)
                                .thumbnail(0.5f)
                                .into(imageView);
                        zjName.setText(namelist.get(index).toString().substring(0, namelist.get(index).indexOf(".")));
                        ZJNAME = namelist.get(index);
                        MPOSITION = index;
                        mCheckMsgHandler.sendEmptyMessageDelayed(MSG_UPDATE_INFO, 100);
                        break;
                    case MSG_UPDATA_BANGDAN:

                        break;
                    case MSG_DELAY_STOP:
                        mDelayStopState = true;
                        break;
                }
            }
        };
    }

    public void saveBangdan() {
        if (ZJNameList.size() > 0) {
            try {
                DataBaseManager.getInstance(MainActivity.this).clearAllData(DBhelper.TABLE_LEFTNAMEINFO);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < ZJNameList.size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("uid", i);
                contentValues.put("name", ZJNameList.get(i));
                try {
                    long m = DataBaseManager.getInstance(MainActivity.this).insetData(DBhelper.TABLE_LEFTNAMEINFO, contentValues);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DataBaseManager.getInstance(MainActivity.this).close();
            ZJNameList.clear();
            Log.e("iiii", "saveBangdan -----");
        }

    }

    public void initBangDanInfo() {
        if (ZJNameList == null) {
            return;
        }
        Log.e("iiii", "initBangDanInfo");
        Cursor cursor = null;
        try {
            cursor = DataBaseManager.getInstance(MainActivity.this).queryData(DBhelper.TABLE_LEFTNAMEINFO, null
                    , null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor.isFirst()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            ZJNameList.add(name);
        }
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            ZJNameList.add(name);
        }
        DataBaseManager.getInstance(MainActivity.this).close();
        bangAdapter.notifyDataSetChanged();


    }


    class bangdanAdapter extends RecyclerView.Adapter<bangdanAdapter.myViewHolder> {


        @Override
        public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            myViewHolder holder = new myViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.bangdan_item
                    , parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(myViewHolder holder, int position) {
            holder.nametext.setText(ZJNameList.get(position).toString().substring(0
                    , ZJNameList.get(position).toString().indexOf(".")));
            String url = SDPATH + ZJNameList.get(position);
            File f = new File(url);
            Glide.with(MainActivity.this)
                    .load(f)
                    .thumbnail(0.3f)
                    .into(holder.imageTX);
        }

        @Override
        public int getItemCount() {

            return ZJNameList.size();
        }

        class myViewHolder extends RecyclerView.ViewHolder {
            TextView nametext;
            ImageView imageTX;

            public myViewHolder(View itemView) {
                super(itemView);
                nametext = (TextView) itemView.findViewById(R.id.bangdanMZ);
                imageTX = (ImageView) itemView.findViewById(R.id.bangdanTX);
            }
        }
    }

    private void test() {

        File file0 = new File("/sdcard/choujiang/choujiang_list.xls");
        List<String> result = new ArrayList<String>();
        try {
            Workbook book = Workbook.getWorkbook(file0);
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows();
            System.out.println("rows: " + rows);
            String lastName = "";
            String list = "HW-冯旭.jpg, 1:sw_余明江.jpg, 2:HW-陈兴春.jpg, 3:SWD+朱静.jpg, 4:SWD 刘琰华.jpg, 5:SWD_熊建勤.jpg, 6:SCD-刘继远.jpg, 7:VAL+张新雷.jpg, 8:HWD+王根在.jpg, 9:VAL+陈绪春.jpg, 10:B&O韩亚萍.jpg, 11:SWD-唐亮.jpg, 12:VAL+王敏.jpg, 13:田春丽.jpg, 14:SWD-易金玉.jpg, 15:B&O王文琴.jpg, 16:HWD+石文平.jpg, 17:SWD+张利可.jpg, 18:PJM王琼苓.jpg, 19:VAL+梁慕贞.jpg, 20:SCD-颜健荣.jpg, 21:MED+廖华英.jpg, 22:NPI+李山茂.jpg, 23:SWD-彭忠柱.jpg, 24:MED+张美玲.jpg, 25:SWD-刘振权.jpg, 26:SWD-邹高向.jpg, 27:HW-黄帆.jpg, 28:B&O陈妍.jpg, 29:唐亮.jpg, 30:谢志恒.jpg, 31:SWD+徐芳.jpg, 32:HWD+刘明亮.jpg, 33:SW_王聪.jpg, 34:SWD-金书婧.jpg, 35:NPI+计伟.jpg, 36:SWD_黄至诚.jpg, 37:SWD+李楠.jpg, 38:HW-徐健华.jpg, 39:EPD-孔庆芳.jpg, 40:B&O刘雪.jpg, 41:B&O颜文娟.jpg, 42:DU+robin.jpg, 43:SWD_陈统考.jpg, 44:VAL+牛学陈.jpg, 45:SWD_邓振盛.jpg, 46:SWD_潘田.jpg, 47:SWD-周龙.jpg, 48:NPI+陈林.jpg, 49:SWD_冯路平.jpg, 50:GPP+代秋平.jpg, 51:SW_万沙金.jpg, 52:SWD_黎奋.jpg, 53:GPP+胡艳燕.jpg, 54:GPP+玉丹妮.jpg, 55:SW_秦美.jpg, 56:吴建宏.jpg, 57:MED+周发兴.jpg, 58:SWD-郭樟库.jpg, 59:SWD-黄文龙.jpg, 60:SWD+周育城.jpg, 61:VAL+杨丰 .jpg, 62:VAL+丁何华.jpg, 63:SWD-黄晶.jpg, 64:VAL+陈丹辉.jpg, 65:ASD+马记涛.jpg, 66:HWD+刘彩霞.jpg, 67:SWD-朱细霞.jpg, 68:HW-胡碧丰.jpg, 69:NPI+庄捷.jpg, 70:VAL+朱国楚.jpg, 71:SWD-叶泽加.jpg, 72:HW+宁玉铭.jpg, 73:GPP+于艳丽.jpg, 74:VAL+陈梦露.jpg, 75:SWD_黎植.jpg, 76:PJM孙玲.jpg, 77:ASD+邱珠伟.jpg, 78:GPP+陈良.jpg, 79:HW-邹方绍.jpg, 80:VAL+代付香.jpg, 81:SWD-向成明.jpg, 82:SWD-吴文锋.jpg, 83:HWD+郑玉森.jpg, 84:MED-陈雪祥.jpg, 85:SWD-廖宽龙.jpg, 86:SWD-杨波.jpg, 87:HWD+田永荣.jpg, 88:SWD 王礼鸿.jpg, 89:VAL+唐文友.jpg, 90:余莲1.jpg, 91:SWD-罗发林.jpg, 92:SWD-秦昌勋.jpg, 93:VAL+王少芳.jpg, 94:SWD-唐冲.jpg, 95:SWD_邓富文.jpg, 96:SWD-赵森林.jpg, 97:PJM朱超.jpg, 98:B&O叶艳.jpg, 99:PA张冠润.jpg, 100:VAL+夏稳.jpg, 101:VAL+朱旭荣.jpg, 102:SWD_周颖.jpg, 103:B&O王蓉.jpg, 104:SWD-刘立林.jpg, 105:GPP+李成.jpg, 106:SWD_史磊.jpg, 107:SWD-袁定.jpg, 108:VAL+肖金.jpg, 109:SW_魏雅芳.jpg, 110:VAL+庄伟.jpg, 111:HWD-颜春霞.jpg, 112:余莲2.jpg, 113:DU+Jefferson.jpg, 114:SWD-樊瑞.jpg, 115:SWD_管湘.jpg, 116:SWD_曾青.jpg, 117:VAL+周谦.jpg, 118:GPP+雷莉娜.jpg, 119:SWD-龚振华.jpg, 120:SWD_刘俊.jpg, 121:ASD+罗亮.jpg, 122:NPI+洪金车.jpg, 123:VAL+王研.jpg, 124:SCD-卢远宇.jpg, 125:李万枝.jpg, 126:HW-张兆生.jpg, 127:SW_张国良.jpg, 128:SWD-庞伟珍.jpg, 129:GPP+伍亮亮.jpg, 130:ASD+熊攀伟.jpg, 131:SW_詹洪.jpg, 132:SWD_刘琼.jpg, 133:VAL+周敬莹.jpg, 134:SW_姚当.jpg, 135:SWD-胡田.jpg, 136:VAL+王亚乐.jpg, 137:HW+吴雄才.jpg, 138:VAL+肖姣.jpg, 139:SWD_张梦琴.jpg, 140:GPP+宋琴.jpg, 141:HWD-李英翠.jpg, 142:SW_杨乔.jpg, 143:SWD-颜玉.jpg, 144:SWD-尤凯.jpg, 145:SWD-曾颂超.jpg, 146:SWD-王杰.jpg, 147:SWD_曾资平.jpg, 148:HWD+梁琰彬.jpg, 149:HW+夏西平.jpg, 150:VAL+刘信.jpg, 151:SWD  肖瑶.jpg, 152:SWD-黄科.jpg, 153:SCD-吴喜彬.jpg, 154:VAL+云永祯.jpg, 155:VAL+邓蕾.jpg, 156:SWD-黎飞平.jpg, 157:B&O汪淑香.jpg, 158:SWD-温进.jpg, 159:孙振东.jpg, 160:PJM张娜娜.jpg, 161:HWD+牛松林.jpg, 162:SWD_叶宗宝.jpg, 163:SCD-陈彬浩.jpg, 164:SWD-陈征鼎.jpg, 165:GPP+陈名.jpg, 166:SWD_吴晓琦.jpg, 167:SWD_罗石勇.jpg, 168:VAL+余录迷.jpg, 169:赵冰.jpg, 170:VAL+王又兴.jpg, 171:VAL+庄秋霞.jpg, 172:SWD_赵峰.jpg, 173:PA庄佳丽.jpg, 174:SWD-chunhua.chen.jpg, 175:SWD_刘莎莎.jpg, ";

            for (int i = 1; i < 201; i++) {
                try {
                    Cell cell_3 = sheet.getCell(2, i);// 获取第列的数据
                    String name = cell_3.getContents();

                    lastName = lastName + "," + name;
                    if (!list.contains(name)) {
                        result.add(name);
                    }

                } catch (Exception e) {
                }
            }
            System.out.println(lastName);
            System.out.println("end");
            book.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < result.size(); k++) {
            sb.append(result.get(k)).append(";");
        }
        Log.d("kaer", "size: " + result.size() + ", name result: " + sb.toString());
    }

}