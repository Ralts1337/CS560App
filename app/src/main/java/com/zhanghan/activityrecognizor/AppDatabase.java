package com.zhanghan.activityrecognizor;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.IDNA;
import android.inputmethodservice.Keyboard;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "testDatabase";   //database name
    private static final String TABLE_NAME = "testTable";           //table name
    private static final int DATABASE_Version = 1;                 // Database Version
    private static final String TIME = "Time";                       //Column I Time(Primary Key)
    private static final String FREQUENCY= "Frequency";             // Column II  Frequency
    private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
            "("+TIME+" VARCHAR(30),"+ FREQUENCY + " VARCHAR(20)" +")";
    private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
    private Context context;

    public AppDatabase (Context context){
        super(context,DATABASE_NAME,null,DATABASE_Version);
    }

    //Create database. called when database is created
    @Override
    public void onCreate (SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE);
        //db.execSQL("Create table if not exists userInformation(id Integer primary key autoincrement, username varchar(20), time varchar(30),frequency varchar(20))");
    }
    //Update database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);//create database again after dropping
        Message.message(context,"OnUpgrade");

    }

    public void insertData(RowOfData row){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TIME,row.getTime());
        values.put(FREQUENCY,row.getFrequency());

        //Now insert row
        db.insert(TABLE_NAME,null,values);
        db.close();//close the db after inserting
    }

    public List<RowOfData> getAllData(){
        List<RowOfData> dataList= new ArrayList<RowOfData>();
        //select All
        String selectQuery = "SELECT * FROM "+ TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        //loop all rows and add to list
        if(cursor.moveToFirst()){
            do{
                RowOfData data = new RowOfData();
                data.setTime(cursor.getString(0));
                data.setFrequency(cursor.getString(1));
                dataList.add(data);
            }while(cursor.moveToNext());
        }
        return dataList;
    }


}


