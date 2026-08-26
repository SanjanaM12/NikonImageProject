package com.example.nikonimageproject;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraFtpServer {
    private FtpServer server;

    //starts connection to the FTP server
    public void start(File storageDirectory, int port) throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory(); //takes care of network management

        factory.setPort(2221);

        //Configures camera credentials to login
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

        //starts the server
        server = serverFactory.createServer();
        server.start();
    }

    public void stop() {
        //stops server if server is running
        if (server != null && !server.isStopped()){
            server.stop();
        }
    }
}
