package com.example.nikonimageproject;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class NikonPtpServer {
    private static final String TAG = "NikonPtpServer";
    public static final int PTP_PORT = 15470;

    private ServerSocket serverSocket;
    private boolean isRunning = false;

    private final byte[] hostGuid = new byte[16];

    //Constructor that creates unique id for the Nikon GUID
    public NikonPtpServer(){
        ByteBuffer bb = ByteBuffer.wrap(hostGuid);
        UUID uuid = UUID.randomUUID();
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
    }

    public void start() {
        isRunning = true;
        //background thread
        new Thread(() -> {
            try {
                //directs network traffic to this port
                serverSocket = new ServerSocket(PTP_PORT);
                Log.i(TAG, "PTP/IP Server listening on port " + PTP_PORT);

                //waits until client initiates a TCP connection
                while (isRunning){
                    Socket socket = serverSocket.accept();
                    Log.i(TAG, "Incoming connection from camera: " + socket.getInetAddress());
                }
            } catch (IOException e) {
                if (isRunning){
                    Log.e(TAG, "Server error: " + e.getMessage());
                }
            }
        }).start(); //creates worker thread to manage specific client socket
    }

    public void stop(){
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
    }
}
