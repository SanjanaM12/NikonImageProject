package com.example.nikonimageproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    //Control to connect networking subsystem
    public NetworkManager(Context context){
        this.connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void bindToWifi(){
        if (connectivityManager == null) {
            Log.i(TAG, "ConnectivityManager unavailable");
            return;
        }

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback(){
            //when Wifi is detected and ready
            @Override
            public void onAvailable(@NonNull Network network){
                super.onAvailable(network);
                //bind socket traffic from app to wifi network
                boolean success = connectivityManager.bindProcessToNetwork(network);
                Log.i(TAG, "Bound process to Wifi network: " + network + " | Success: " + success);
            }

            @Override
            public void onLost(@NonNull Network network){
                super.onLost(network);
                Log.w(TAG, "Wifi network connection lost - Unbinding process");
                //Unbind if wifi network drops
                connectivityManager.bindProcessToNetwork(null);
            }
        };

        try {
            connectivityManager.requestNetwork(request, networkCallback);
            Log.i(TAG, "Network request submitted for Wifi binding");
        } catch (Exception e) {
            Log.e(TAG, "Failed to request network: " + e.getMessage(), e);
        }
    }
}
