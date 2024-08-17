package com.shahzavi.android.photogallery;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

public class PhotoGalleryActivity extends SingleFrangmentActivity{
    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
    public static Intent newIntent(Context context)
    {
        return new Intent(context, PhotoGalleryActivity.class);
    }
}
