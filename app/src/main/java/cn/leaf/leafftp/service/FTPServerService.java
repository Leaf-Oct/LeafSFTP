package cn.leaf.leafftp.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.impl.LIST;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.impl.DefaultConnectionConfig;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.leaf.leafftp.LeafApplication;
import cn.leaf.leafftp.R;
import cn.leaf.leafftp.activity.MainActivity;
import cn.leaf.leafftp.sharedpreferences.Config;

public class FTPServerService extends IntentService {
    FtpServerFactory serverFactory = new FtpServerFactory();
    FtpServer server;
    Config config = Config.getInstance();

    public FTPServerService() {
        super("FTPServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FtpServerFactory factory = new FtpServerFactory();
        Log.i("factort", "created");
        List<Authority> writable = new ArrayList<>();
        writable.add(new WritePermission());
        if (config.leaf_mode) {
            BaseUser in = new BaseUser();
            in.setName("in");
            in.setPassword("in");
            in.setHomeDirectory(config.inner_storage_path);
            in.setEnabled(true);
            in.setAuthorities(writable);
            try {
                factory.getUserManager().save(in);
            } catch (FtpException e) {
                e.printStackTrace();
                Toast.makeText(LeafApplication.getContext(), "保存in账户失败", Toast.LENGTH_SHORT).show();
                Log.e("in 账户", "保存失败");
            }
            Log.i("in", "added");
            if (config.has_SD_card) {
                BaseUser sd = new BaseUser();
                sd.setName("sd");
                sd.setPassword("sd");
                sd.setHomeDirectory(config.SD_card_path);
                sd.setEnabled(true);
                sd.setAuthorities(writable);
                try {
                    factory.getUserManager().save(sd);
                } catch (FtpException e) {
                    e.printStackTrace();
                    Toast.makeText(LeafApplication.getContext(), "保存sd账户失败", Toast.LENGTH_SHORT).show();
                    Log.e("sd 账户", "保存失败");
                }
                Log.i("sd", "added");
            }

        } else {
            BaseUser anonymous = new BaseUser();
            anonymous.setName("anonymous");
            anonymous.setPassword("");
            anonymous.setHomeDirectory(config.custom_path);
            anonymous.setAuthorities(writable);
            anonymous.setEnabled(true);
            try {
                factory.getUserManager().save(anonymous);
            } catch (FtpException e) {
                e.printStackTrace();
                Toast.makeText(LeafApplication.getContext(), "保存匿名账户失败", Toast.LENGTH_SHORT).show();
                Log.e("匿名账户", "保存失败");
            }
            Log.i("anonymous", "added");
        }
        ListenerFactory listener_factory = new ListenerFactory();
        listener_factory.setPort(config.port);
//        @TODO 配置SSL会报版本异常
        factory.addListener("default", listener_factory.createListener());
        Log.i("listener", "added");
//        写死的配置
        factory.setConnectionConfig(new ConnectionConfig() {
            private final DefaultConnectionConfig default_config = new DefaultConnectionConfig();

            @Override
            public int getMaxLoginFailures() {
                return default_config.getMaxLoginFailures();
            }

            @Override
            public int getLoginFailureDelay() {
                return default_config.getLoginFailureDelay();
            }

            @Override
            public int getMaxAnonymousLogins() {
                return 10;
            }

            @Override
            public int getMaxLogins() {
                return 10;
            }

            @Override
            public boolean isAnonymousLoginEnabled() {
                return true;
            }

            @Override
            public int getMaxThreads() {
                return 20;
            }
        });
        server = factory.createServer();
        Log.i("server", "get");
        Notification n=createForegroundNotification();
        startForeground(1, n);
        try {
            synchronized (this) {
                server.start();
                Log.i("server", "run");
                EventBus.getDefault().post(Boolean.TRUE);
                wait();
                Log.i("server", "stopppppp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LeafApplication.getContext(), "FTP Server boot failed", Toast.LENGTH_SHORT).show();
            Log.e("ftp server", "boot fail");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
        server = null;
        stopForeground(true);
        Log.i("server", "stop");
        EventBus.getDefault().post(Boolean.FALSE);
    }

    private Notification createForegroundNotification(){
        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        var channel_id="ftp server";
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            var channel_name="ftp server notification";
            var notification_channel=new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            notification_channel.setDescription("means ftp service is running");
            if(manager!=null){
                manager.createNotificationChannel(notification_channel);
            }
        }
        Notification.Builder notification_builder=null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notification_builder=new Notification.Builder(this, channel_id);
        }
        else {
            notification_builder=new Notification.Builder(this);
        }
        notification_builder.setSmallIcon(R.drawable.power_on);
        notification_builder.setContentTitle("Leaf FTP Server");
        notification_builder.setContentText("running");
        return notification_builder.build();
    }
}