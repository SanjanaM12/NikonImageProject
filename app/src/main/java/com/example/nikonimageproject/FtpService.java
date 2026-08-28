package com.example.nikonimageproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

public class FtpService extends Service implements NikonPtpServer.OnPhotoReceivedListener {
    private static final String TAG = "FTPService";
    private static final String CHANNEL_ID = "nikon_ptp_channel";
    private static final int NOTIFICATION_ID = 101;
    private NikonPtpServer ptpServer;

    @Override
    public void onCreate(){
        super.onCreate();
        ptpServer = new NikonPtpServer();
    }

    @Override
    //allows UI to call function inside background process directly
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //entry point whenever a startService is called
    public int onStartCommand(Intent intent, int flags, int startID) {
       ptpServer.start();
       return START_STICKY; //allows for continuous background server
    }

    @Override
    public void onPhotoReceived(File file) {
        Log.i(TAG, "New photo successfully received: " + file.getAbsolutePath());
    }

    @Override
    //final process - cleanup phase
    public void onDestroy() {
        super.onDestroy(); //base Service cleanup
        if (ptpServer != null){
            ptpServer.stop();
        }
    }
}
