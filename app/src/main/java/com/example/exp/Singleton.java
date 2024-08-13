package com.example.exp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class Singleton {
    
    private static Singleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;
    private ImageLoader imageLoader;
    private RequestQueue rq;

    public Singleton(Context ctx) {
        Singleton.ctx = ctx;
        rq = Volley.newRequestQueue(ctx.getApplicationContext());

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });


    }



    public static synchronized Singleton getInstance(Context context) {
        if (instance == null) {
            instance = new Singleton(context);
        }
        return instance;
    }

    public void cancelRequests(){
        rq.cancelAll(request -> true);
    }

    public RequestQueue getRequestQueue() {
        if (rq == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            rq = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return rq;
    }
    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
    
}
