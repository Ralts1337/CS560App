package com.zhanghan.activityrecognizor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLiteManager_BackUpCopy extends SQLiteOpenHelper {
    public SQLiteManager_BackUpCopy(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super (context, name, factory, version);
    }

    public SQLiteManager_BackUpCopy(Context context, String name)
    {
        this(context, name, null,1);
    }
    //Create database
    @Override
    public void onCreate (SQLiteDatabase db)
    {
        db.execSQL("Create table if not exists userInformation(id Integer primary key autoincrement, username varchar(20), time varchar(30),frequency varchar(20))");
    }
    //Update database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

}


