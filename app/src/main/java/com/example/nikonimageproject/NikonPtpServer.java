package com.example.nikonimageproject;

import android.util.Log;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class NikonPtpServer {
    private static final String TAG = "NikonPtpServer";
    public static final int PTP_PORT = 15740;

    private static final int PTPIP_INIT_COMMAND_REQ = 1;
    private static final int PTPIP_INIT_COMMAND_ACK = 2;
    private static final int PTPIP_INIT_EVENT_REQ = 3;
    private static final int PTPIP_INIT_EVENT_ACK = 4;
    private static final int OPERATION_REQ = 6;
    private static final int OPERATION_RESP = 7;
    private static final int EVENT = 8;
    private static final int START_DATA = 9;
    private static final int DATA_PACKET = 10;
    private static final int END_DATA = 12;
    private static final int PTP_OP_OPEN_SESSION = 0x1002;
    private static final int PTP_RESP_OK = 0x2001;

    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private int connectionCounter = 1000;

    private final byte[] hostGuid = new byte[16];

    //Constructor that creates unique id for the Nikon GUID
    public NikonPtpServer(){
        ByteBuffer bb = ByteBuffer.wrap(hostGuid);
        UUID uuid = UUID.randomUUID();
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
    }

    public interface OnPhotoReceivedListener {
        void onPhotoReceived(File file);
    }

    private OnPhotoReceivedListener listener;

    public void setOnPhotoReceivedListener(OnPhotoReceivedListener listener){
        this.listener = listener;
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
            //binary I/O streams
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //runs until wifi disconnected or all files finish transferring
            while (isRunning && !socket.isClosed()) {
                //reads 4 bytes to covert them to Java int
                byte[] lenBuf = new byte[4];
                in.readFully(lenBuf);
                int length = ByteBuffer.wrap(lenBuf).order(ByteOrder.LITTLE_ENDIAN).getInt();

                if (length < 8) break;

                //reads 4 bytes to identify the packet type
                byte[] typeBuf = new byte[4];
                in.readFully(typeBuf);
                int packetType = ByteBuffer.wrap(typeBuf).order(ByteOrder.LITTLE_ENDIAN).getInt();

                byte[] payload = new byte[length - 8];
                in.readFully(payload);

                Log.i(TAG, "Received Packet: Type=" + packetType + " | Length=" + length);

                //route packet based on type
                switch (packetType) {
                    case PTPIP_INIT_COMMAND_REQ:
                        handleInitCommand(out, payload);
                        break;
                    case PTPIP_INIT_EVENT_REQ:
                        handleInitEvent(out);
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

    private void handleInitCommand(DataOutputStream out, byte[] payload) throws IOException {
        connectionCounter++;
        String hostName = "AndroidReceiver\0";
        byte[] nameBytes = hostName.getBytes("UTF-16LE");

        int totalLen = 8 + 4 + 16 + nameBytes.length + 4; //header + connNum + GUID + name + version
        ByteBuffer buffer = ByteBuffer.allocate(totalLen).order(ByteOrder.LITTLE_ENDIAN); //Allocates RAM and format
        buffer.putInt(totalLen);
        buffer.putInt(PTPIP_INIT_COMMAND_ACK);
        buffer.putInt(connectionCounter);
        buffer.put(hostGuid);
        buffer.put(nameBytes);
        buffer.putInt(0x00010000);

        //Adds array to socket output stream and sends ti
        out.write(buffer.array());
        out.flush();
        Log.i(TAG, "Sent Init_Command_Ack (Assigned Conn ID: " + connectionCounter +")");
    }

    private void handleInitEvent(DataOutputStream out) throws IOException {
        int totalLen = 8;
        ByteBuffer buffer = ByteBuffer.allocate(totalLen).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(totalLen);
        buffer.putInt(PTPIP_INIT_EVENT_ACK);

        out.write(buffer.array());
        out.flush();
        Log.i(TAG, "Sent Init_Event_Ack. Handshake complete");
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
