package com.example.nikonimageproject;

import android.app.Service; //background process and functionality for other applications
import android.content.Intent; //for operation to be performed
import android.os.IBinder; //protocol for interacting with a remote object

public class FtpService extends Service {

    @Override
    //allows UI to call function inside background process directly
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //entry point whenever a startService is called
    public int onStartCommand(Intent intent, int flags, int startID) {
        return START_STICKY; //allows for continuous background server
    }

    @Override
    //final process - cleanup phase
    public void onDestroy() {
        super.onDestroy(); //base Service cleanup
    }
}
