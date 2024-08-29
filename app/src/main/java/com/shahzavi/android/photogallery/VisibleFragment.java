package com.shahzavi.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "Visible Fragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter=new IntentFilter(PollService1.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification,intentFilter,PollService1.PREM_PRIVATE,null);//only my app can trigger it the app that as this permission
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
    private BroadcastReceiver mOnShowNotification=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"cancelling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
