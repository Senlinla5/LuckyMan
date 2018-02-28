package com.tcl.choujiang.choujiang.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by senlin on 2018/1/20.
 */

public class DBhelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "choujiangDB";
    private static final int DATABASE_VERSION = 5;
    public static final String TABLE_PRIZEINFO = "prize_info";
    public static final String TABLE_NAMEINFO = "name_info";
    public static final String TABLE_REMOVENAMEINFO = "removename_info";
    public static final String TABLE_LEFTNAMEINFO = "leftname_info";
    private static final String CREATE_PRIZEINFO_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_PRIZEINFO
            + " (_id Integer primary key autoincrement," + " uid integer," + " prizename text, "+ " last text, "+"count text);";//karer
    private static final String CREATE_NAMEINFO_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAMEINFO
            + " (_id Integer primary key autoincrement," + " uid integer," + " name text);";
    private static final String CREATE_REMOVENAMEINFO_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_REMOVENAMEINFO
            + " (_id Integer primary key autoincrement," + " uid integer," + " prizelevel text," + " name text);";
    private static final String CREATE_LEFTNAMEINFO_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_LEFTNAMEINFO
            + " (_id Integer primary key autoincrement," + " uid integer," + " name text);";

    public DBhelper(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_NAMEINFO_SQL);
        sqLiteDatabase.execSQL(CREATE_PRIZEINFO_SQL);
        sqLiteDatabase.execSQL(CREATE_REMOVENAMEINFO_SQL);
        sqLiteDatabase.execSQL(CREATE_LEFTNAMEINFO_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
