package com.vdusar.radien.logic.database_manager;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;

public class DatabaseManagerUtil {
    private final DatabaseHelperUtility DB ;
    private final Context ctx;

    public static final String Id_C_Name = "ID";
    public static final String Channel_C_Name = "Channel";
    public static final String N_C_Title = "Title";
    public static final String DownloadId_C_Name = "DownloadId";
    public static final String DownloadState_C_Name = "DownloadState";
    public static final String DownloadCondition_C_Name = "DownloadCondition";


    public DatabaseManagerUtil(Context context){
        this.ctx=context;
        this.DB = new DatabaseHelperUtility(context,"radioDB.db",1);
    }

    public void createChannelTable(String channelId){
        String query = "CREATE TABLE IF NOT EXISTS '" + channelId + "' ("
                + Id_C_Name + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Channel_C_Name + " TEXT,"
                + N_C_Title + " TEXT,"
                + DownloadState_C_Name + " TEXT,"
                + DownloadCondition_C_Name + " TEXT,"
                + DownloadId_C_Name + " TEXT)";
        DB.exeQuery(query);
    }

    public void appendValueInTable(String ChannelId,String ChannelName,String Title,String DownloadId,String state,String condition){
        ContentValues t_values = new ContentValues();
        t_values.put(Channel_C_Name,ChannelName);
        t_values.put(N_C_Title,Title);
        t_values.put(DownloadId_C_Name,DownloadId);
        t_values.put(DownloadState_C_Name,state);
        t_values.put(DownloadCondition_C_Name,condition);
        DB.insertTableValues(ChannelId,t_values);
    }



    public ArrayList<ContentValues> getChannelDownloads(String channelId) {
        String query = "SELECT * from '" + channelId+"'";
        if (DB.checkTableExist(channelId)){
            return DB.getQuery(query);
        }else {
            return null;
        }
    }

    public boolean checkTableEmpty(String id){
        return DB.checkTableIsEmpty(id);
    }


    public void updateDownloadingState(String state,String channelId,String rowId){
        ArrayList<ContentValues> v = getChannelDownloads(channelId);
        if (v!=null){
            String query = "UPDATE '"+channelId+"' SET "+DownloadState_C_Name+"='"+state+"' WHERE "+Id_C_Name+"='"+rowId+"'";
            DB.exeQuery(query);
        }
    }

    public void removeDownloadFromDatabase(String channelId,String rowId){
        if (DB.checkTableExist(channelId)){
            String query = "delete from '"+channelId+"' where "+Id_C_Name+"='"+rowId+"';";
            DB.exeQuery(query);
        }
    }

    public void removeChannelDownloads(String channelId){
        String query = "Drop Table If Exists '"+channelId+"'";
        DB.exeQuery(query);
    }

    public ArrayList<String> getAllChannel(){
        return DB.getAllTables();
    }

}
