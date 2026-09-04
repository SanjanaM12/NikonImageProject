package com.example.nikonimageproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


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
    }

    //start background service
    private void startPtpService() {
        Intent serviceIntent = new Intent(this, FtpService.class);
        startForegroundService(serviceIntent);
    }

    //runs when user navigates back to screen
    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.nikonimageproject.NEW_PHOTO");
        registerReceiver(photoReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

    }

    //runs when user navigates away from screen
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(photoReceiver);
    }
}