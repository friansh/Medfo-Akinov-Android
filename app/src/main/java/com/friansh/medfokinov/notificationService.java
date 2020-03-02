package com.friansh.medfokinov;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class notificationService extends Service {
    private void setRequestCount(String count){
        SharedPreferences requestCount = getApplicationContext().getSharedPreferences(getString(R.string.REQUEST_COUNT_PREF_ID), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = requestCount.edit();
        editor.putString( getString(R.string.REQUEST_COUNT_PREF_ID), count);
        editor.apply();
    }

    public void getRequestCount() {
        RequestQueue queue = Volley.newRequestQueue(notificationService.this);
        String login_url = getString(R.string.REST_API_HOST);
        final notificationService self = notificationService.this;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, login_url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            SharedPreferences rc = getApplicationContext().getSharedPreferences(getString(R.string.REQUEST_COUNT_PREF_ID), Context.MODE_PRIVATE);
                            String requestCount =  rc.getString(getString(R.string.REQUEST_COUNT_PREF_ID), "error");

                            if (!requestCount.equals("error")){
                                if (!requestCount.equals(json.getString("data"))) {
                                    showNotification();
                                }
                            }

                            setRequestCount(json.getString("data"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("token", "medfoAkinov2020");
                params.put("action", "berapa");

                return params;
            }
        };

        queue.add(stringRequest);
    }

    void showNotification(){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, EditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.NOTIFICATION_CHANNEL_ID))
                .setSmallIcon(R.mipmap.medfokinov)
                .setContentTitle(getString(R.string.NOTIFICATION_CONTENT_TITLE))
                .setContentText("Daftar publikasi berubah, silahkan cek aplikasi...")
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setDefaults(Notification.DEFAULT_SOUND) //Important for heads-up notification
                .setPriority(Notification.PRIORITY_MAX); //Important for heads-up notification

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.NOTIFICATION_CHANNEL_NAME);
            String description = getString(R.string.NOTIFICATION_CHANNEL_DESCRIPTION);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.NOTIFICATION_CHANNEL_ID), name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setVibrationPattern(new long[]{100, 100, 100, 100, 100, 100, 100, 100});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createForegroundChannel(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.FOREGROUND_CHANNEL_NAME);
            String description = getString(R.string.FOREGROUND_CHANNEL_DESCRIPTION);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.FOREGROUND_CHANNEL_ID), name, importance);
            channel.setDescription(description);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[] {0, 0});
            channel.setSound(null, null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        createNotificationChannel();
        createForegroundChannel();
        Log.d("frianshLog", "Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, getString(R.string.FOREGROUND_CHANNEL_ID))
                .setSmallIcon(R.mipmap.medfokinov)
                .setContentTitle(getString(R.string.NOTIFICATION_CONTENT_TITLE))
                .setContentText("Aplikasi berjalan, menerima notifikasi.")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(12, notification);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("frianshLog", "Service stopped");
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.d("frianshLog", "Async started");
            while (true) {
                try {
                    Thread.sleep(1000);
                    getRequestCount();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected void onPostExecute(String result) {

        }


        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onProgressUpdate(String... text) {
        }
    }
}