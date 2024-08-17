package com.shahzavi.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class PollService extends IntentService {
    private static final String ChannelId="PollService";
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60;

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        Log.i(TAG, "Recieved an intent: " + intent);
        String lastResultId = QueryPreferences.getLastResultId(this);
        String url;
        String query = QueryPreferences.getStoredQuery(this);
        if (query == null)
            url = PhotoFetch.getUrlString("" + 1, null);
        else
            url = PhotoFetch.getUrlString("" + 1, query);
        new PhotoFetch().fetchItems(this, url, new PhotoFetch.VolleyCallback() {
            @Override
            public void onSuccess(List<GalleryItem> items) {
                if (items.isEmpty()) {
                    return;
                }
                String resultId = items.get(0).getId();
                if (resultId.equals(lastResultId)) {
                    Log.i(TAG, "got old result:" + resultId);
                } else {
                    Log.i(TAG, "got new result:" + resultId);
                    Resources resources = getResources();
                    Intent i = PhotoGalleryActivity.newIntent(PollService.this);
                    PendingIntent pi = PendingIntent.getActivity(PollService.this, 0, i, PendingIntent.FLAG_IMMUTABLE);
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(PollService.this);
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                    {
                        NotificationChannel notificationChannel=new NotificationChannel(ChannelId,"PhotoGallery", NotificationManager.IMPORTANCE_DEFAULT);
                        notificationManagerCompat.createNotificationChannel(notificationChannel);
                    }
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(PollService.this,ChannelId)
                            .setTicker(resources.getString(R.string.new_picture_title))
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentTitle(resources.getString(R.string.new_picture_title))
                            .setContentText(resources.getString(R.string.new_picture_text))
                            .setContentIntent(pi)
                            .setAutoCancel(true);
                    if (ActivityCompat.checkSelfPermission(PollService.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.d(TAG,"Permission not granted");
                        return;
                    }
                    Log.d(TAG,"notifycalled");
                    notificationManagerCompat.notify(0, notificationBuilder.build());
                }

                QueryPreferences.setLastResultId(PollService.this,resultId);
            }
        });

    }
    public static void setServiceAlarm(Context context,boolean isOn)
    {
        Intent i=PollService.newIntent(context);
        PendingIntent pi=PendingIntent.getService(context,0,i,  PendingIntent.FLAG_MUTABLE);
        Log.d(TAG,"PI Id :"+pi);
        AlarmManager alarmManager=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            Log.d(TAG,"AlarmManager created");
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),POLL_INTERVAL,pi);}
        else {
            Log.d(TAG,"AlarmManager desttroyed");
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
    public static boolean isServiceAlarmOn(Context context)
    {
        Intent i=PollService.newIntent(context);
        PendingIntent pi=PendingIntent.getService(context,0,i, PendingIntent.FLAG_NO_CREATE| PendingIntent.FLAG_MUTABLE);
        Log.d(TAG,"PI Id :"+pi);
        return pi!=null;
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable=cm.getActiveNetworkInfo()!=null;
        boolean isNetworkConnected=isNetworkAvailable&&cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    public PollService() {
        super(TAG);

    }

}

