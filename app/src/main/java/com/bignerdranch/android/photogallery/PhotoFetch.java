package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PhotoFetch {
    public interface VolleyCallback{
        void onSuccess(List<GalleryItem> items);
    }
    List<GalleryItem> mItems=new ArrayList<>();
    private final static String TAG="PhotoFetch";
    private final static String API_KEY="45094413-6eff673412b75b9c8c59e6ecc";
    private static final Uri END_POINT=Uri.parse("https://pixabay.com/api/")
            .buildUpon()
            .appendQueryParameter("key",API_KEY)
            .appendQueryParameter("per_page","100")
            .build();
   public static String getUrlString(String page_no,String query){
    Uri.Builder uriBuilder=END_POINT.buildUpon().appendQueryParameter("page",page_no);
                 if(query!=null)
                   uriBuilder.appendQueryParameter("q",query);
                return uriBuilder.build().toString();
    }
    public byte[] getUrlBytes(String urlSpec) throws IOException//Downloading the bytes of data stored at the url
    {
        URL url=new URL(urlSpec);
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        try
        {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in=connection.getInputStream();
            if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+":with"+urlSpec);
            }
            int bytesRead=0;
            byte[] buffer=new byte[1024];
            while((bytesRead=in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

   public void fetchItems(Context context,String url,final VolleyCallback callback)
    {
        List<GalleryItem> mitems=new ArrayList<>();
        JsonObjectRequest jsonArrayRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {//https://jsonplaceholder.typicode.com/todos/1
                try {
                   parseItems(mitems,response);
                   callback.onSuccess(mitems);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"error occured");
            }
        });
        jsonArrayRequest.setTag(PollService1.TAG);
        SingletonClass.newInstance(context).getRequestQueue().add(jsonArrayRequest);

    }
    private void parseItems(List<GalleryItem> items, JSONObject response)throws  JSONException
    {
        JSONArray jsonArray=response.getJSONArray("hits");
        for(int i=0;i<jsonArray.length();i++){
            JSONObject jsonObject=jsonArray.getJSONObject(i);
            GalleryItem galleryItem=new GalleryItem();
            galleryItem.setCaption(jsonObject.getString("tags"));
            galleryItem.setId(jsonObject.getString("id"));
            galleryItem.setUrl(jsonObject.getString("pageURL"));
            items.add(galleryItem);
           }

    }
}
