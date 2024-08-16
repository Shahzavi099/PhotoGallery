package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private final static String TAG="ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD=0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloaderListener;
    private ConcurrentHashMap<T,String> mConcurrentHashMap=new ConcurrentHashMap<>();
    interface ThumbnailDownloadListener<T>{
        void onThumbnailDownload(T target, Bitmap thumbnail);
    }
    public void setThumbnailDownloaderListener(ThumbnailDownloadListener<T> listener)
    {
        mThumbnailDownloaderListener=listener;
    }
    public ThumbnailDownloader(Handler responseHandler ) {
        super(TAG);//you need to send the name of the handler to the handlerthread class
        mResponseHandler=responseHandler;//to get mainthread handler
    }
    public void queueThumbnail(T target,String url) //to create message and then send to the messagequeue of thumnaildownloaderthread
    {
        Log.i(TAG,"Got URL"+url);
        if(url==null)
            mConcurrentHashMap.remove(target);
        else{ mConcurrentHashMap.put(target,url);
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();}//added to the messagequeue
    }
    public void clearQueue()
    {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    @Override
    protected void onLooperPrepared() {//this method gets called after start() method only after thhis method you can create a handler object
        super.onLooperPrepared();
        mRequestHandler=new Handler(getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what==MESSAGE_DOWNLOAD){
                    T target=(T)msg.obj;
                    handleReqest(target);
                }
            }
        };
    }
    private void handleReqest(final T target)
    {
        try {
            final String url = mConcurrentHashMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new PhotoFetch().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {//it will swnd this runnable object to the messagequeue of the mainthread and this method will be executed when pulled out
                @Override
                public void run() {
                    if(mConcurrentHashMap.get(target)!=url)//checks whether the Image's url is the same or not as recyclerview recycles its photoholder
                        return;
                    mConcurrentHashMap.remove(target);
                    mThumbnailDownloaderListener.onThumbnailDownload(target,bitmap);//caling the onThumbnaildownload method overriden in the photogalleryfragment
                }
            });
        }
        catch (IOException ioe)
        {
            Log.e(TAG,"Error downloading");

        }
    }
}
