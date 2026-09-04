package com.example.nikonimageproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
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
    private boolean isServerRunning = false;

    //mDNS service registration for camera pairing
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannel();

        ptpServer = new NikonPtpServer();
        ptpServer.setOnPhotoReceivedListener(this);

        nsdManager = (NsdManager) getSystemService((Context.NSD_SERVICE));
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        //Guard against duplicate binds on port 15740
        if (!isServerRunning) {
            ptpServer.start();
            isServerRunning = true;
            Log.i(TAG, "PTP Server started in foreground service.");
        }
        return START_STICKY; //allows for continuous background server
    }

    //Advertises app/device as a Nikon Wireless Transmitter Utility over mDNS
    private void registerNsdService() {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("Nikon Wireless Receiver");
        serviceInfo.setServiceType("_nikon-wtu._tcp.");
        serviceInfo.setPort(15740);

        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                Log.i(TAG, "mDNS Service successfully registered: " + nsdServiceInfo.getServiceName());
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.i(TAG, "mDNS registration failed: " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo){
                Log.i(TAG, "mDNS Service unregistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "mDNS unregistration failed: " + errorCode);
            }
        };

        if (nsdManager != null) {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        }
    }

    @Override
    public void onPhotoReceived(File file) {
        Log.i(TAG, "New photo received: " + file.getAbsolutePath());

        Intent intent = new Intent("com.example.nikonimageproject.NEW_PHOTO");
        intent.putExtra("image_path", file.getAbsolutePath());
        sendBroadcast(intent);
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
