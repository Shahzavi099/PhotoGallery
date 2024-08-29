package com.shahzavi.android.photogallery;

import android.net.Uri;

import androidx.annotation.NonNull;

public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;
    private String pageUrl;

    @NonNull
    @Override
    public String toString() {
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Uri getPageUrl() {
        return Uri.parse(pageUrl);
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
}
