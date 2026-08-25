package com.example.nikonimageproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FtpService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
