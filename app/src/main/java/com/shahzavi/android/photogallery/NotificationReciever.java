package com.shahzavi.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReciever extends BroadcastReceiver {
    private static final String TAG = "NotificationReciever ";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"recieved result "+getResultCode());//BroadcastReciever method Retrieve the current result code, as set by the previous receiver.
        if(getResultCode()!= Activity.RESULT_OK)
            return;
        Notification notification=intent.getParcelableExtra(PollService1.NOTIFICATION);
        int requestCode=intent.getIntExtra(PollService1.REQUEST_CODE,0);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Permission not granted");
            return;
        }
        Log.i(TAG,"Notified");
        NotificationManagerCompat.from(context).notify(requestCode,notification);
    }
}
