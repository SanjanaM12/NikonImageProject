package com.example.nikonimageproject;

import android.util.Log;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class NikonPtpServer {
    private static final String TAG = "NikonPtpServer";
    public static final int PTP_PORT = 15470;

    private static final int PTPIP_INIT_COMMAND_REQ = 1;
    private static final int PTPIP_INIT_COMMAND_ACK = 2;

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
                    new Thread(() -> handleConnection(socket)).start();
                }
            } catch (IOException e) {
                if (isRunning){
                    Log.e(TAG, "Server error: " + e.getMessage());
                }
            }
        }).start(); //creates worker thread to manage specific client socket
    }

    public void handleConnection(Socket socket){
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (isRunning && !socket.isClosed()) {
                byte[] lenBuf = new byte[4];
                in.readFully(lenBuf);
                int length = ByteBuffer.wrap(lenBuf).order(ByteOrder.LITTLE_ENDIAN).getInt();

                if (length < 0) break;

                byte[] typeBuf = new byte[4];
                in.readFully(typeBuf);
                int packetType = ByteBuffer.wrap(typeBuf).order(ByteOrder.LITTLE_ENDIAN).getInt();

                byte[] payload = new byte[length - 8];
                in.readFully(payload);

                Log.i(TAG, "Received Packet: Type=" + packetType + " | Length=" + length);

                switch (packetType) {
                    case PTPIP_INIT_COMMAND_REQ:
                        break;
                    case PTPIP_INIT_COMMAND_ACK:
                        break;
                    default:
                        Log.i(TAG, "Received Data/Command packet (Type: " + packetType + ")");
                        break;
                }
            }
        } catch (IOException e ) {
            Log.w(TAG, "Camera socket disconnected: " + e.getMessage());
        }
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
