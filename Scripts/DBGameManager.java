package com.example.user.fatsim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBGameManager extends SQLiteOpenHelper {

    public static final String GAME_TABLE = "GAME_STATE";

    //게임 데이터
    public static final String GAME_KEY_ID = "_ID";
    public static final String GAME_KEY_NAME = "NAME";
    public static final String GAME_KEY_CLICK = "CLICK";
    public static final String GAME_KEY_AUTO = "AUTO";
    public static final String GAME_KEY_BG = "BACKGROUND";

    private static final String GAME_TABLE_CREATE = "CREATE TABLE " + GAME_TABLE +"(" +
            GAME_KEY_ID + " INTEGER PRIMARY KEY DEFAULT 1," +
            GAME_KEY_NAME + " TEXT UNIQUE NOT NULL, " +
            GAME_KEY_CLICK + " LONG NOT NULL DEFAULT 100," +
            GAME_KEY_AUTO + " LONG NOT NULL DEFAULT 500," +
            GAME_KEY_BG + " INTEGER NOT NULL DEFAULT 1)";

    private static final String DATABASE_NAME = "FOH.db";
    private static final int DATABASE_VERSION = 1;

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBGameManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public DBGameManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB 생성
    // onCreate() 함수는 생성자에서 넘겨받은 이름과 버전의 데이터베이스가 존재하지 않을때 한번 호출
    @Override
    public void onCreate(SQLiteDatabase db) {
        // id, 이름, 클릭, auto, bg
        db.execSQL(GAME_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV){

        db.execSQL("DROP TABLE IF EXISTS " + GAME_TABLE);

        onCreate(db);
    }

    public void checkGame(String name){

        SQLiteDatabase sdb = this.getReadableDatabase();

        Cursor game_cursor;

        game_cursor = sdb.rawQuery(search(GAME_KEY_ID, GAME_KEY_NAME, name, -1), null);

        if(game_cursor.getCount() == 0)
            insertGAME(name, 500, 1300, 1);

        game_cursor.close();

    }


    public boolean insertGAME(String name, long click, long auto, int bg){

        SQLiteDatabase sb = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        try{
            contentValues.put(GAME_KEY_NAME, name);
            contentValues.put(GAME_KEY_CLICK, click);
            contentValues.put(GAME_KEY_AUTO, auto);
            contentValues.put(GAME_KEY_BG, bg);
        } catch (SQLiteException e){
            return false;
        }

        //실패시 -1 반환
        long result = sb.insert(GAME_TABLE, null, contentValues);

        if(result == -1) return false;
        else return true;
    }


    public void updateGame(String setWhat, String how, String withWhat, String inWhere){
        SQLiteDatabase sdb = this.getWritableDatabase();

        switch(how) {
            case "+":
            case "++":
            case "add":
            case "ADD":
            case "Add":
                sdb.execSQL("update " + GAME_TABLE + " set " + setWhat + " = "
                        + setWhat + " + " + withWhat + " where " + GAME_KEY_NAME + " = " + "\"" + inWhere + "\"");
                break;
            case "-":
            case "dec":
            case "minus":
            case "decrease":
            case "--":
                sdb.execSQL("update " + GAME_TABLE + " set " + setWhat + " = "
                        + setWhat + " - " + withWhat + " where " + GAME_KEY_NAME + " = " + "\"" + inWhere + "\"");
                break;
            case "*":
            case "mul":
            case "mult":
            case "multiple":
                sdb.execSQL("update " + GAME_TABLE + " set " + setWhat + " = "
                        + setWhat + " * " + withWhat + " where " + GAME_KEY_NAME + " = " + "\"" + inWhere + "\"");
                break;
            case "/":
            case "div":
            case "divide":
                sdb.execSQL("update " + GAME_TABLE + " set " + setWhat + " = "
                        + setWhat + " / " + withWhat + " where " + GAME_KEY_NAME + " = " + "\"" + inWhere + "\"");
                break;
        }
    }

    public String getGameByKey(String value, String what){
        SQLiteDatabase sdb = this.getReadableDatabase();
        Cursor cursor = sdb.rawQuery("select " + value + " from " + GAME_TABLE + " where " + GAME_KEY_NAME + " = " + "\"" + what + "\"", null);
        String str = "";
        cursor.moveToFirst();

        if(!cursor.isAfterLast()){
            str = cursor.getString(0);
        }

        cursor.close();

        return str;
    }

    public String search(String select, String where, String what, long intWhat){
        String str = "";
        if(intWhat == -1){
            str = "select " + select + " from " + GAME_TABLE + " where " + where + " = " + "\"" + what + "\"";
        } else {
            str = "select " + select + " from " + GAME_TABLE + " where " + where + " = " + intWhat;
        }
        return str;
    }

    public String printData(String tableName) {

        SQLiteDatabase db = getReadableDatabase();
        String str = "";

        // id, 클릭당, auto, exp, bg
        Cursor cursor = db.rawQuery("select * from " + GAME_TABLE, null);

        while(cursor.moveToNext()) {
            str += cursor.getInt(0)
                    + "), ID ("
                    + cursor.getString(1)
                    + "), click ("
                    + cursor.getLong(2)
                    + "), auto ("
                    + cursor.getLong(3)
                    + "), BG ("
                    + cursor.getLong(4)
                    + ")" + "\n";
        }

        cursor.close();
        return str;
    }

    public void deleteTable(){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from " + GAME_TABLE);
    }

    public void dropTable(){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("drop table " + GAME_TABLE);
    }
}

