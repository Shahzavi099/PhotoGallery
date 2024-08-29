package com.shahzavi.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class PhotoPageActivity extends SingleFrangmentActivity{

    @Override
    public Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());//to fectch uri
    }
    public static Intent newIntent(Context context, Uri photoPageUri)
    {
        Intent i=new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

}
