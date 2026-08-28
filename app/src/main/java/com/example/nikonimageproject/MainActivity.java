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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

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

            String ip = getDeviceIpAddress();
            tvIpAddress.setText("Server Running\nIP:" + ip + "\nPort: 2221");
        });

        btnStopServer.setOnClickListener(v -> {
            //Creates Intent targeting FTPService to trigger onDestroy and stop server
            Intent stopIntent = new Intent(this, FtpService.class);
            stopService(stopIntent);
            tvIpAddress.setText("Server: Stopped");
        });
    }

    private String getDeviceIpAddress() {
        try {
            //get all network interfaces
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                //get all ip addresses of this interface
                List<InetAddress> addresses = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addresses) {
                    //filter out loopback (home) and IPv6 addresses (select only Ipv4)
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        String host = addr.getHostAddress();
                        if (host.startsWith("192.168.43.") || host.startsWith("192.168.49.")) {
                            return host;
                        }
                    }

                }
            }

            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addresses = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addresses) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        //Defauly if no active wifi interface found
        return "127.0.0.1";
    }
}