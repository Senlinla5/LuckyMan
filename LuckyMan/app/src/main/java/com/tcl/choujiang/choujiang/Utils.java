package com.tcl.choujiang.choujiang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.tcl.choujiang.choujiang.util.DBhelper;
import com.tcl.choujiang.choujiang.util.DataBaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final boolean DBG = true;
    public static Object object = new Object();
    public static List mIndexArray = new ArrayList<Integer>();

    public static int randomNumber(int count) {
        int i = (int) (Math.random() * count);
        return i;
    }

    public static List<String> disruptOrder(List<String> list) {
        int count = list.size();
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            for (int dbg = 0; dbg < count; dbg++) {
                sb.append(dbg).append(":").append(list.get(dbg)).append(", ");
            }
            Log.d("karer", "list all: " + sb.toString());
        }

        List<String> newList = new ArrayList<String>();
        newList.clear();
        if (DBG) Log.d("karer", "mIndexArray: " + mIndexArray.size());
        mIndexArray.clear();
        for (int i = 0; i < count; i++) {
            //生成随机数
            int j = Utils.randomNumber(count);
            if (DBG) Log.d("karer", "disruptOrder, j: " + j);
            if (mIndexArray.contains(j)) {
                i--;
                if (DBG) Log.d("karer", "disruptOrder, i--");
                continue;
            }
            newList.add(i, list.get(j));
            mIndexArray.add(j);
            if (DBG) Log.d("karer", "disruptOrder, i: " + i + ", j: " + j);
        }
        if (DBG) {
            StringBuilder sb2 = new StringBuilder();
            for (int d = 0; d < mIndexArray.size(); d++) {
                sb2.append(mIndexArray.get(d)).append(", ");
            }
            Log.d("karer", "mIndexArray all: " + sb2.toString());
        }
        if (DBG) {
            StringBuilder sb3 = new StringBuilder();
            for (int dbg2 = 0; dbg2 < newList.size(); dbg2++) {
                sb3.append(dbg2).append(":").append(newList.get(dbg2)).append(", ");
            }
            Log.d("karer", "newList all: " + sb3.toString());
        }
        return newList;
    }

    public static int queryNumber(Context context, String prizelevel) {
        Cursor cursor = null;
        String selection = "prizelevel=?";
        String[] selecttionArgs = new String[]{prizelevel};
        String last = "";
        try {
            cursor = DataBaseManager.getInstance(context).queryData(DBhelper.TABLE_REMOVENAMEINFO, null, selection
                    , selecttionArgs, null, null, null, null);
            Log.d("karer", "cursor : " + cursor);
            if (cursor != null) {
                Log.d("karer", "cursor size: " + cursor.getCount());
                return cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataBaseManager.getInstance(context).close();
        }
        return 0;
    }

    public static String queryCount(Context context, String prizeName) {
        Cursor cursor = null;
        String selection = "prizename=?";
        String[] selecttionArgs = new String[]{prizeName};
        String count = "0";
        try {
            cursor = DataBaseManager.getInstance(context).queryData(DBhelper.TABLE_PRIZEINFO, null, selection
                    , selecttionArgs, null, null, null, null);
            Log.d("karer", "cursor : " + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getString(cursor.getColumnIndex("count"));
                Log.d("karer", "count : " + count);
                return count != null ? count : "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataBaseManager.getInstance(context).close();
        }
        return "0";
    }

    public static void insertAllDataBase(Context context, List<String> nameList) {
        try {
            DataBaseManager.getInstance(context).clearAllData(DBhelper.TABLE_NAMEINFO);
            Log.d("karer", "insertAllDataBase - namelist size: " + nameList.size());
            for (int i = 0; i < nameList.size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("uid", i);
                contentValues.put("name", nameList.get(i));
                long k = DataBaseManager.getInstance(context).insetData(DBhelper.TABLE_NAMEINFO, contentValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataBaseManager.getInstance(context).close();
        }
    }
}
