package com.example.nikonimageproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

public class FtpService extends Service implements CameraFtpServer.OnPhotoReceivedListener{
    private static final String TAG = "FTPService";
    private CameraFtpServer ftpServer;
    private NetworkManager networkManager;

    @Override
    public void onCreate(){
        networkManager = new NetworkManager(this);
    }

    @Override
    //allows UI to call function inside background process directly
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //entry point whenever a startService is called
    public int onStartCommand(Intent intent, int flags, int startID) {
        //Pin socket routing to camera Wifi hotspot
        if (networkManager != null){
            networkManager.bindToWifi();
        }

        //creates the path for the incoming images
        File incomingDir = new File(getExternalFilesDir(null), "incoming");
        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }

        //creates a new Server and starts it
        ftpServer = new CameraFtpServer(this);
        try {
            ftpServer.start(incomingDir, 2221);
            Log.i(TAG, "FTP Server started on port 2221");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start FTP Server: " + e.getMessage(), e);
        }

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
        if (ftpServer != null){
            ftpServer.stop();
        } //stops server if it was running
        if (networkManager != null){
            networkManager.unbindFromWifi();
        }

    }
}
