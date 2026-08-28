package com.example.nikonimageproject;

import java.nio.ByteBuffer;
import java.util.UUID;

public class NikonPtpServer {
    private static final String TAG = "NikonPtpServer";
    public static final int PTP_PORT = 15470;

    private final byte[] hostGuid = new byte[16];

    //Constructor that creates unique id for the Nikon GUID
    public NikonPtpServer(){
        ByteBuffer bb = ByteBuffer.wrap(hostGuid);
        UUID uuid = UUID.randomUUID();
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
    }
}
