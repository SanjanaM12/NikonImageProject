package com.example.nikonimageproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private TextView tvIpAddress;
    private Button btnStartServer;
    private Button btnStopServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Links Java variables to their XML layout counterparts by ID
        tvIpAddress = findViewById(R.id.tvIpAddress);
        btnStartServer = findViewById(R.id.btnStartServer);
        btnStopServer = findViewById(R.id.btnStopServer);

        //Applies system bar insets as padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnStartServer.setOnClickListener(v -> {
            Intent startIntent = new Intent(this, FtpService.class);
            startService(startIntent);
        });

        btnStopServer.setOnClickListener(v -> {
            //Creates Intent targeting FTPService to trigger onDestroy and stop server
            Intent stopIntent = new Intent(this, FtpService.class);
            stopService(stopIntent);
            tvIpAddress.setText("Server: Stopped");
        });

    }
}