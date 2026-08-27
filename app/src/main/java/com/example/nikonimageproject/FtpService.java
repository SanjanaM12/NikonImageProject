package com.example.nikonimageproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;

public class FtpService extends Service {
    private CameraFtpServer ftpServer;

    @Override
    //allows UI to call function inside background process directly
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //entry point whenever a startService is called
    public int onStartCommand(Intent intent, int flags, int startID) {
        //creates the path for the incoming images
        File incomingDir = new File(getExternalFilesDir(null), "incoming");
        if (!incomingDir.exists()) {
            incomingDir.mkdir();
        }

        //creates a new Server and starts it
        ftpServer = new CameraFtpServer((CameraFtpServer.OnPhotoReceivedListener) this);
        try {
            ftpServer.start(incomingDir, 2221);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY; //allows for continuous background server
    }

    @Override
    //final process - cleanup phase
    public void onDestroy() {
        super.onDestroy(); //base Service cleanup
        if (ftpServer != null){
            ftpServer.stop();
        } //stops server if it was running
    }
}
