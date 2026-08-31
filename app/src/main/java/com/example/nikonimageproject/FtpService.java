package com.example.nikonimageproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class FtpService extends Service implements NikonPtpServer.OnPhotoReceivedListener {
    private static final String TAG = "FTPService";
    private static final String CHANNEL_ID = "nikon_ptp_channel";
    private static final int NOTIFICATION_ID = 101;
    private NikonPtpServer ptpServer;

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannel();

        ptpServer = new NikonPtpServer();
        ptpServer.setOnPhotoReceivedListener(this);
    }

    @Override
    //allows UI to call function inside background process directly
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //entry point whenever a startService is called
    public int onStartCommand(Intent intent, int flags, int startID) {
        //Keeps service alive in foreground
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nikon Wireless Link Active")
                .setContentText("Listening for camera on port 15740...")
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        ptpServer.start();
        return START_STICKY; //allows for continuous background server
    }

    @Override
    public void onPhotoReceived(File file) {
        Log.i(TAG, "New photo received: " + file.getAbsolutePath());
    }

    @Override
    //final process - cleanup phase
    public void onDestroy() {
        super.onDestroy(); //base Service cleanup
        if (ptpServer != null){
            ptpServer.stop();
        }
    }

    private void createNotificationChannel() {
        //Registers a channel for system notifications (silent notification)
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "NikonCameraReceiver", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
