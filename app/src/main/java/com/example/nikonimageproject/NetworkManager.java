package com.example.nikonimageproject;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkManager {
    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    //Control to connect networking subsystem
    public NetworkManager(Context context){
        this.connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
