package com.example.nikonimageproject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView ivLatestPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Links Java variables to their XML layout counterparts by ID
        ivLatestPhoto = findViewById(R.id.ivLatestPhoto);

        Intent serviceIntent = new Intent(this, FtpService.class);
        startForegroundService(serviceIntent);
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