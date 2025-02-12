package com.vdusar.radien.logic.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.vdusar.radien.logic.singleton.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

public class volleyRequestData
{
    Context ctx;
    VolleySingleton inst;

    public volleyRequestData(Context context){

        this.ctx=context;
        this.inst= VolleySingleton.getInstance(this.ctx);
    }

    private static final String TAG = "log";

    public interface VolleyCallback{
        void returnJsonArray(JSONArray result);
    }

    public interface getImageCallback{
        void returnImageBitmap(Bitmap img);
    }

    public interface advGetImageCallback{
        void returnImageBitmap(Bitmap img,int id);
    }

    public interface VolleyCallbackObj{
        void returnJsonObj(JSONObject result);
    }

    public interface VolleyCallbackHTTP{
        void returnHTTPObj(String result);
    }


    public void findCountryRadios(Integer hm, Integer pg,String cnt,VolleyCallbackObj callme) {
        String how_many = Integer.toString(hm);
        String which_page = Integer.toString(pg);

        String data_url = "https://zeno.fm/api/stations/?query=&limit=" + how_many + "&genre=&country="+cnt+"&language=&page=" + which_page;

        sendRequestObj(data_url, callme);
    }

    public void sendArrayRequest(String url, volleyRequestData.VolleyCallback callback) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, (response) -> {
                    callback.returnJsonArray(response);
                }
                        ,error -> {
                    Log.e(TAG,"Error in send Request"+error.toString());
                });

        inst.addToRequestQueue(jsonArrayRequest);

    }

    public void getImage(String uri,getImageCallback callme){
        ImageRequest imageRequest=new ImageRequest (uri, response -> callme.returnImageBitmap(response),0,0, ImageView.ScaleType.CENTER_CROP,null, (error)->{
            callme.returnImageBitmap(null);
            System.out.println("Error in getting the Radio Theme Work");
        });

        inst.addToRequestQueue(imageRequest);
    }

    public void AdvGetImage(String uri,int id,advGetImageCallback callme){
        ImageRequest imageRequest=new ImageRequest (uri, response -> callme.returnImageBitmap(response,id),0,0, ImageView.ScaleType.CENTER_CROP,null, (error)->{
            callme.returnImageBitmap(null,0);
            System.out.println("Error in getting the Radio Theme Work");
        });
        inst.addToRequestQueue(imageRequest);
    }

    public void getHttpString(String uri,VolleyCallbackHTTP callme){
        StringRequest httpRequest = new StringRequest(Request.Method.GET,uri,(resp)->{
            callme.returnHTTPObj(resp);
        },(error)->{
            callme.returnHTTPObj(null);
        });
        inst.addToRequestQueue(httpRequest);
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
        inst.addToRequestQueue(jsonObjectRequest);
    }

    public void cancelAllRequest(){
        inst.cancelRequests();
    }


}
