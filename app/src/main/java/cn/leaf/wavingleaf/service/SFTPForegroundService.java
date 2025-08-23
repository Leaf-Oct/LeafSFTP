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

import androidx.annotation.Nullable;

import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cn.leaf.wavingleaf.database.UserDao;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.sharedpreferences.Config;

public class SFTPForegroundService extends Service {

    SshServer sshd;
    final Config config = Config.getInstance();
    UserDao dao;
    NotificationManager manager;
    private static final String TAG = "SFTPForegroundService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "sftp_service_channel";
    public int port;
    private final IBinder binder = new LocalBinder();
    ResultReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        dao = UserDatabaseSingleton.getInstance(this).getUserDao();
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
        port = intent.getIntExtra("port", 2222);
        if (receiver==null){
            receiver=intent.getParcelableExtra("receiver");
        }
        try {
            startSFTP();
        } catch (Exception e) {
            if (receiver!=null){
                var b=new Bundle();
                b.putString("msg", "SFTP Service 启动失败");
                receiver.send(-1, b);
            }
            e.printStackTrace();
            stopSelf();
            return START_STICKY;
        }
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY_COMPATIBILITY;
    }

    private void startSFTP() throws Exception {
        sshd = new ServerBuilder().build(true);
        sshd.setPort(port);
        sshd.setHost("0.0.0.0");
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get(new File(getExternalFilesDir(null), "key").toURI())));
        sshd.setSignatureFactories(new ArrayList<>(BuiltinSignatures.VALUES));
        sshd.setPasswordAuthenticator((username, password, session) -> {
            var pwd = dao.getPwdFromSFTPUser(username);
            if (pwd == null) {
                return false;
            }
            return password.equals(pwd) && dao.getEnableStatusFromSFTPUser(username);
        });
        sshd.setFileSystemFactory(new FileSystemFactory() {
            @Override
            public Path getUserHomeDir(SessionContext session) {
                var user = session.getUsername();
                var home = dao.getHomeFromSFTPUser(user);
                return home == null ? null : Paths.get(home);
            }

            @Override
            public FileSystem createFileSystem(SessionContext session) throws IOException {
                var user = session.getUsername();
                var home = dao.getHomeFromSFTPUser(user);
                return home == null ? null : new VirtualFileSystemFactory(Paths.get(home)).createFileSystem(session);
            }
        });
        sshd.setSubsystemFactories(List.of(new SftpSubsystemFactory()));

        synchronized (this) {
            sshd.start();
            config.sftp_is_running = true;
        }
        if (receiver!=null){
            receiver.send(1, null);
        }
    }


    private void stopSFTP() {
        if (sshd != null && !sshd.isClosed()) {
            try {
                sshd.stop();
            } catch (IOException e) {
                Log.i(TAG, "Error stopping SFTP server", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSFTP();
        synchronized (config) {
            config.sftp_is_running = false;
        }
//        EventBus.getDefault().post(new SFTPStatusSwitchEvent());
//        c.update();
        if (receiver!=null){
            receiver.send(1, null);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public SFTPForegroundService getService() {
            return SFTPForegroundService.this;
        }
    }

}
