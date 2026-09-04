package com.example.nikonimageproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    private ImageView ivLatestPhoto;
    private TextView tvIpAddress;
    private Button btnStartServer;
    private Button btnStopServer;

    private final BroadcastReceiver photoReceiver = new BroadcastReceiver() {
        //Catch event and display photo
        @Override
        public void onReceive(Context context, Intent intent) {
            String imagePath = intent.getStringExtra("image_path");
            if(imagePath != null){
                //Decode file and display on UI
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                ivLatestPhoto.setImageBitmap(bitmap);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Links Java variables to their XML layout counterparts by ID
        ivLatestPhoto = findViewById(R.id.ivLatestPhoto);
        tvIpAddress = findViewById(R.id.tvIpAddress);
        btnStartServer = findViewById(R.id.btnStartServer);
        btnStopServer = findViewById(R.id.btnStopServer);

        btnStartServer.setOnClickListener(v -> startPtpService());
        btnStopServer.setOnClickListener(v -> stopPtpService());

        displayLocalIp();
    }

    //start background service
    private void startPtpService() {
        Intent serviceIntent = new Intent(this, FtpService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        displayLocalIp();
    }

    //stop background service
    private void stopPtpService(){
        Intent serviceIntent = new Intent(this, FtpService.class);
        stopService(serviceIntent);
        tvIpAddress.setText("Server: Stopped");
    }

    private void displayLocalIp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            String ipString = Formatter.formatIpAddress(ipAddress);
            tvIpAddress.setText("IP: " + ipString + ":15740");
        }
    }

    //runs when user navigates back to screen
    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.nikonimageproject.NEW_PHOTO");
        ContextCompat.registerReceiver(this, photoReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    //runs when user navigates away from screen
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(photoReceiver);
    }
}