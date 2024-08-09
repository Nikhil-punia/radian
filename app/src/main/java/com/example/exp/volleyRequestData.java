package com.example.exp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.exp.ui.adapter.recyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class volleyRequestData
{
    Context ctx;

    public volleyRequestData(Context context){
        this.ctx=context;
    }

    private static final String TAG = "log";

    public interface VolleyCallback{
        void returnJsonArray(JSONArray result);
    }

    public interface getImageCallback{
        void returnImageBitmap(Bitmap img);
    }

    public interface VolleyCallbackObj{
        void returnJsonObj(JSONObject result);
    }


    public void findCountryRadios(Integer hm, Integer pg,String cnt,VolleyCallback callme) {
        String how_many = Integer.toString(hm);
        String which_page = Integer.toString(pg);

        String data_url = "https://zeno.fm/api/stations/?query=&limit=" + how_many + "&genre=&country="+cnt+"&language=&page=" + which_page;

        sendRequest(data_url, callme);
    }

    public void sendRequest(String url,volleyRequestData.VolleyCallback callback) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, (response) -> {
                    callback.returnJsonArray(response);
                }
                        ,error -> {
                    Log.e(TAG,"Error in send Request"+error.toString());
                });

        Singleton.getInstance(this.ctx).addToRequestQueue(jsonArrayRequest);

    }

    public void getImage(String uri,getImageCallback callme){
        ImageRequest imageRequest=new ImageRequest (uri, response -> callme.returnImageBitmap(response),0,0, ImageView.ScaleType.CENTER_CROP,null, (error)->{
            callme.returnImageBitmap(null);
            System.out.println("Error in getting the Radio Theme Work");
        });

        Singleton.getInstance(this.ctx).addToRequestQueue(imageRequest);
    }


    public void sendRequestObj(String url,VolleyCallbackObj callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, (response) -> {
                    System.out.println(response);
                    callback.returnJsonObj(response);
                }
                        ,(error) -> {
                    System.out.println("Error in send Request Object"+error.toString());
                    callback.returnJsonObj(null);
                });
        Singleton.getInstance(this.ctx).addToRequestQueue(jsonObjectRequest);
    }


}
