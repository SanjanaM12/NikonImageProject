package com.example.nikonimageproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private ImageView ivLatestPhoto;

    private final BroadcastReceiver photoReceiver = new BroadcastReceiver() {
        //Catch event and display photo
        @Override
        public void onReceive(Context context, Intent intent) {
            String imagePath = intent.getStringExtra("image_path");
            if(imagePath != null){
                //Decode file and display on UI
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
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

        //Start background service
        Intent serviceIntent = new Intent(this, FtpService.class);
        startForegroundService(serviceIntent);
    }
}