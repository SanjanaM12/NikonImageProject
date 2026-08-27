package com.example.nikonimageproject;

import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraFtpServer {
    private static final String TAG = "CameraFTPServer";
    private FtpServer server;
    private final OnPhotoReceivedListener listener;

    //interface to send file paths back to UI
    public interface OnPhotoReceivedListener {
        void onPhotoReceived(File file);
    }

    //Server constructor with reference
    public CameraFtpServer(OnPhotoReceivedListener listener) {
        this.listener = listener;
    }


    //starts connection to the FTP server
    public void start(File storageDirectory, int port) throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory(); //takes care of network management

        factory.setPort(port);
        serverFactory.addListener("default", factory.createListener());

        //Configures camera credentials to log in
        BaseUser user = new BaseUser();
        user.setName("Nikon");
        user.setPassword("Nikon");
        user.setHomeDirectory(storageDirectory.getAbsolutePath());

        //Configures write permissions to save photos to tablet
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);

        UserManager userManager = serverFactory.getUserManager();
        userManager.save(user);

        //Ensures file has been sent and uploaded
        Map<String, Ftplet> ftplets = new HashMap<>();
        ftplets.put("photoListener", new PhotoUploadFtplet(storageDirectory, listener));
        serverFactory.setFtplets(ftplets);


        //starts the server
        server = serverFactory.createServer();
        server.start();
        Log.i(TAG, "FTP Server listening on port" + port);
    }

    public void stop() {
        //stops server if server is running
        if (server != null && !server.isStopped()){
            server.stop();
            Log.i(TAG, "FTP Server stopped");
        }
    }
    private static class PhotoUploadFtplet extends DefaultFtplet {
        private final File baseDir;
        private final OnPhotoReceivedListener callback;

        //constructor
        public PhotoUploadFtplet(File baseDir, OnPhotoReceivedListener callback) {
            this.baseDir = baseDir;
            this.callback = callback;
        }

        //
        @Override
        public FtpletResult onUploadEnd(FtpSession session, FtpRequest request){
            //creates new file from directory and file name
            String fileName = request.getArgument();
            File newFile = new File(baseDir, fileName);

            //checks for notification of file received and sends
            if (callback != null){
                callback.onPhotoReceived(newFile);
            }

            return FtpletResult.DEFAULT;
        }
    }
}