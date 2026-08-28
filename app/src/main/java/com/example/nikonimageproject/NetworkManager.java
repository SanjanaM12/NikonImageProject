package com.example.nikonimageproject;

import android.content.Context;
import android.util.Log;


public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private final Context context;


    public NetworkManager(Context context){
        this.context = context;
    }

    public void bindToWifi() {
        Log.i(TAG, "Running in Hotspot Host Mode: Ready");
    }

    public void unbindFromWifi() {
        Log.i(TAG, "Hotspot routing released")
    }
}
