package com.vdusar.radien.logic.database_manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelperUtility extends SQLiteOpenHelper {


    public DatabaseHelperUtility(Context ctx, String DB_name,int Version) {
        super(ctx, DB_name, null, Version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {}

    public void exeQuery(String query){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public ArrayList<String> getAllTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name!='android_metadata' AND name!='sqlite_sequence' ";
        Cursor c = db.rawQuery(query, null);
        ArrayList<String> channels = new ArrayList<>();
        if (c!= null) {
            if (c.moveToFirst()) {
                do {
                    if (c.getColumnIndex("name")!=-1) {
                        int index = c.getColumnIndex("name");
                        channels.add(c.getString(index));
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        db.close();
        return channels;
    }

    public ArrayList<ContentValues> getQuery(String query){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor!= null){
        ArrayList <ContentValues> returnValue = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ContentValues rvalue = new ContentValues();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    rvalue.put(cursor.getColumnName(i), cursor.getString(i));
                }
                returnValue.add(rvalue);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return returnValue;
        }else {
            return null;
        }
    }
/* @IIF not supported in lower api */
//    public boolean checkTableExist(String tableName){
//        String query = "SELECT IIF((SELECT EXISTS (SELECT name FROM sqlite_master WHERE type='table' AND name='"+tableName+"')),True,False) result;";
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor data = db.rawQuery(query,null);
//        if (data.moveToFirst()) {
//            if (data.getInt(0)==0){
//                return false;
//            }else{
//                return true;
//            }
//        }else {
//            return false;
//        }
//    }

    public boolean checkTableExist(String tableName) {
        Cursor cursor = this.getReadableDatabase().rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void insertTableValues(String Table_Name,ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("'"+Table_Name+"'", null, values);
        db.close();
    }

    public void replaceTableValues(String Table_Name,ContentValues values){
        SQLiteDatabase db = this.getWritableDatabase();
        db.replace(Table_Name, null, values);
        db.close();
    }

    public void updateTableValue(String Table_Name, ContentValues value, String Id, String IdName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(Table_Name,value,IdName + " = ?", new String[] { Id });
        db.close();
    }

    public void deleteTableValues(String Table_Name,String Id, String IdName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Table_Name, IdName + " = ?", new String[] { Id });
        db.close();
    }

    public boolean checkTableIsEmpty(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM '"+id+"'";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        return icount <= 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}