package com.shahzavi.android.photogallery;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SingletonClass {
    private RequestQueue mRequestQueue;
    private static SingletonClass mInstance;
    private SingletonClass(Context context)
    {
        mRequestQueue= Volley.newRequestQueue(context.getApplicationContext());
    }
    public static synchronized SingletonClass newInstance(Context context)
    {
        if(mInstance==null){
            mInstance=new SingletonClass(context);}
            return mInstance;
    }
    public RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }
}
