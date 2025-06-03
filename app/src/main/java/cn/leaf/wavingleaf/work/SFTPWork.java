package cn.leaf.wavingleaf.work;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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

import cn.leaf.wavingleaf.R;
import cn.leaf.wavingleaf.database.UserDao;
import cn.leaf.wavingleaf.event.SFTPStatusSwitchEvent;
import cn.leaf.wavingleaf.sharedpreferences.Config;

public class SFTPWork extends Worker {

    SshServer sshd;
    Config config = Config.getInstance();
    UserDao dao;
    NotificationManager manager;

    public SFTPWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        System.setProperty("user.home", Environment.getExternalStorageDirectory().getAbsolutePath());
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(dao.getSFTPPort());
        sshd.setHost("0.0.0.0");
//        sshd.setKeyPairProvider(new CustomKeyPairProvider(this));
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get(new File(getApplicationContext().getExternalFilesDir(null), "key").toURI())));

        sshd.setSignatureFactories(new ArrayList<>(BuiltinSignatures.VALUES));

        sshd.setPasswordAuthenticator((username, password, session) -> {
            var pwd=dao.getPwdFromSFTPUser(username);
            if (pwd==null){
//                Log.i("user", "no exist");
                return false;
            }
            var enable=dao.getEnableStatusFromSFTPUser(username);
//            Log.i("username", username);
//            Log.i("password", password);
//            Log.i("pwd in db", pwd);
//            Log.i("enable", enable+"");
//            Log.i("result", Boolean.toString(password.equals(pwd)&&enable));
            return password.equals(pwd)&&enable;

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

        Notification n = createForegroundNotification();
//        startForeground(1, n);
        EventBus.getDefault().post(new SFTPStatusSwitchEvent());
        try {
            synchronized (config) {
                config.is_running = true;
            }
            synchronized (this) {
                sshd.start();
                wait();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Result.success();
    }

    private Notification createForegroundNotification() {


        var channel_id = "sftp server";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel_name = "sftp server notification";
            var notification_channel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            notification_channel.setDescription("means sftp service is running");
            if (manager != null) {
                manager.createNotificationChannel(notification_channel);
            }
        }
        Notification.Builder notification_builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification_builder = new Notification.Builder(getApplicationContext(), channel_id);
        } else {
            notification_builder = new Notification.Builder(getApplicationContext());
        }
        notification_builder.setSmallIcon(R.mipmap.ic_launcher);
        notification_builder.setContentTitle("Waving Leaf SFTP");
        notification_builder.setContentText("running");
        return notification_builder.build();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }


}
