package cn.leaf.wavingleaf.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import cn.leaf.wavingleaf.database.UserDao;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.event.SFTPStatusSwitchEvent;
import cn.leaf.wavingleaf.sharedpreferences.Config;

public class SFTPForegroundService extends Service {

    SshServer sshd;
    Config config = Config.getInstance();
    UserDao dao;
    NotificationManager manager;
    private static final String TAG = "SFTPForegroundService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "sftp_service_channel";
    public int port;
    private final IBinder binder=new LocalBinder();

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
                    "SFTP Server",
                    NotificationManager.IMPORTANCE_LOW
            ));
        }
    }

    private Notification createNotification() {
        String title = "SFTP服务运行中";
        String content = "正在监听端口: "+port;
        Notification.Builder builder;
        builder = new Notification.Builder(this, CHANNEL_ID);
        return builder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        port=intent.getIntExtra("port", 2222);
        try {
            startSFTP();
        } catch (Exception e){
            new AlertDialog.Builder(this).setTitle("error").setMessage(e.getMessage()).create().show();
            stopSelf();
            return START_STICKY;
        }
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY_COMPATIBILITY;
    }

    private void startSFTP() throws Exception{
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setHost("0.0.0.0");
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get(new File(getExternalFilesDir(null), "key").toURI())));
        sshd.setSignatureFactories(new ArrayList<>(BuiltinSignatures.VALUES));
        sshd.setPasswordAuthenticator((username, password, session) -> {
            var pwd=dao.getPwdFromSFTPUser(username);
            if (pwd==null){
                return false;
            }
            return password.equals(pwd)&&dao.getEnableStatusFromSFTPUser(username);
        });
        sshd.setFileSystemFactory(new FileSystemFactory() {
            @Override
            public Path getUserHomeDir(SessionContext session) throws IOException {
                var user = session.getUsername();
                var home=dao.getHomeFromSFTPUser(user);
                return home==null?null:Paths.get(home);
            }

            @Override
            public FileSystem createFileSystem(SessionContext session) throws IOException {
                var user = session.getUsername();
                var home=dao.getHomeFromSFTPUser(user);
                return home==null?null:new VirtualFileSystemFactory(Paths.get(home)).createFileSystem(session);
            }
        });
        sshd.setSubsystemFactories(Arrays.asList(new SftpSubsystemFactory()));

        try {
            synchronized (this) {
                sshd.start();
                config.is_running = true;
            }
            EventBus.getDefault().post(new SFTPStatusSwitchEvent());
        } catch (IOException e) {
            throw e;
        }
    }

    private void stopSFTP(){
        if (sshd != null && !sshd.isClosed()) {
            try {
                sshd.stop();
            } catch (IOException e) {
                Log.e(TAG, "Error stopping SFTP server", e);
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSFTP();
        synchronized (config) {
            config.is_running = false;
        }
        EventBus.getDefault().post(new SFTPStatusSwitchEvent());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder{
        public SFTPForegroundService getService(){
            return SFTPForegroundService.this;
        }
    }

}
