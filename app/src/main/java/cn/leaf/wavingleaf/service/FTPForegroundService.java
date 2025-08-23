package cn.leaf.wavingleaf.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.List;

import cn.leaf.wavingleaf.database.UserDao;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.model.FTPUser;
import cn.leaf.wavingleaf.sharedpreferences.Config;

public class FTPForegroundService extends Service {

    FtpServer server;
    NotificationManager manager;
    Config config = Config.getInstance();
    UserDao dao;
    private static final String CHANNEL_ID="ftp_service_channel";
    private static final int NOTIFICATION_ID = 1002;
    public int port;
    List<FTPUser> list;
    ResultReceiver receiver;

    private final IBinder binder = new FTPForegroundService.LocalBinder();
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        dao= UserDatabaseSingleton.getInstance(this).getUserDao();
    }

    private void createNotificationChannel() {
        manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID,
                    "FTP Server",
                    NotificationManager.IMPORTANCE_LOW
            ));
        }
    }
    private Notification createNotification(){
        String title = "FTP服务运行中";
        String content = "正在监听端口: " + port;
        Notification.Builder builder;
        builder = new Notification.Builder(this, CHANNEL_ID);
        return builder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        port=intent.getIntExtra("port", 2121);
        if (receiver==null){
            receiver=intent.getParcelableExtra("receiver");
        }
        startForeground(NOTIFICATION_ID, createNotification());
        try {
            startFTP();
        } catch (Exception e){
            if (receiver!=null){
                var b=new Bundle();
                b.putString("msg", "FTP Service 启动失败");
                receiver.send(-2, b);
            }
            e.printStackTrace();
            stopSelf();
            return START_STICKY;
        }
        return START_STICKY_COMPATIBILITY;
    }

    private void startFTP(){
        var server_factory=new FtpServerFactory();
        var listener_factory=new ListenerFactory();
        listener_factory.setPort(port);
//        listener_factory.setServerAddress("0.0.0.0");
        server_factory.addListener("default", listener_factory.createListener());
        var writable_permission=new ArrayList<Authority>();
        writable_permission.add(new WritePermission());
        var t=new Thread(()->{
            list=dao.getAllFTPUsers();
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for(var u:list){
            var new_user=new BaseUser();
            new_user.setName(u.user);
            new_user.setPassword(u.password);
            new_user.setHomeDirectory(u.home);
            new_user.setEnabled(u.enable);
            new_user.setAuthorities(u.writable?writable_permission:null);
            try {
                server_factory.getUserManager().save(new_user);
            } catch (FtpException e) {
                throw new RuntimeException(e);
            }
        }
        server=server_factory.createServer();
        synchronized (this){
            try {
                server.start();
                config.ftp_is_running=true;
            } catch (FtpException e) {
                throw new RuntimeException(e);
            }
        }
        if (receiver!=null){
            receiver.send(2, null);
        }
    }

    private void stopFTP(){
        if (server!=null&&!server.isStopped()){
            try {
                server.stop();
                server=null;
            } catch (Exception e){
                Log.i("FTP Server", "Error stopping FTP Server", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFTP();
        synchronized (config){
            config.ftp_is_running=false;
        }
//        c.update();
        if (receiver!=null){
            receiver.send(2, null);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder{
        public FTPForegroundService getService(){
            return FTPForegroundService.this;
        }
    }
}