package com.shahzavi.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class PollService1 extends JobService {
    private static final int JOB_ID=1;
    private static final String ChannelId="PollService";
    public static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60 * 15;
    public static  final String ACTION_SHOW_NOTIFICATION="com.shahzavi.photogallery.SHOW_NOTIFICATION";
    public static  final String PREM_PRIVATE="com.shahzavi.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE="REQUEST_CODE";
    public static final String NOTIFICATION="NOTIFICATION";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "Recieved an intent: " );
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
                    Intent i = PhotoGalleryActivity.newIntent(PollService1.this);
                    PendingIntent pi = PendingIntent.getActivity(PollService1.this, 0, i, PendingIntent.FLAG_IMMUTABLE);
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(PollService1.this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(ChannelId, "PhotoGallery", NotificationManager.IMPORTANCE_DEFAULT);
                        notificationManagerCompat.createNotificationChannel(notificationChannel);
                    }
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(PollService1.this, ChannelId)
                            .setTicker(resources.getString(R.string.new_picture_title))
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentTitle(resources.getString(R.string.new_picture_title))
                            .setContentText(resources.getString(R.string.new_picture_text))
                            .setContentIntent(pi)
                            .setAutoCancel(true);
                    showBackgroundNotification(0, notificationBuilder.build());// the app  that have this permission can recieve this broadcast intent
                }
                QueryPreferences.setLastResultId(PollService1.this,resultId);
                jobFinished(jobParameters, false);
            }
        });
return true;
    }
    private void showBackgroundNotification(int requestCode, Notification notification)
    {
        Intent i=new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE,requestCode);
        i.putExtra(NOTIFICATION,notification);
        sendOrderedBroadcast(i,PREM_PRIVATE,null,null, Activity.RESULT_OK,null,null);
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        SingletonClass.newInstance(this).getRequestQueue().cancelAll(TAG);
        return true;
    }
    public static void setJobScheduler(Context context,boolean isOn) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (isOn) {
            Log.d(TAG,"job set");
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollService1.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(POLL_INTERVAL)
                    .setPersisted(true)
                    .build();
            jobScheduler.schedule(jobInfo);
        }
        else {
            jobScheduler.cancel(JOB_ID);
            Log.d(TAG,"job cancel");
        }
    }
    public static boolean isJobSchedulerOn(Context context)
    {
        JobScheduler scheduler=(JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled=false;
        for(JobInfo jobinfo:scheduler.getAllPendingJobs())
        {
            if(jobinfo.getId()==JOB_ID)
                hasBeenScheduled=true;
        }
        return hasBeenScheduled;


}
}
