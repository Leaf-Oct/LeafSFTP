package cn.leaf.leafsftp.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
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
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


import cn.leaf.leafsftp.R;
import cn.leaf.leafsftp.database.UserDao;
import cn.leaf.leafsftp.database.UserDatabaseSingleton;
import cn.leaf.leafsftp.event.SFTPStatusSwitchEvent;
import cn.leaf.leafsftp.sharedpreferences.Config;

public class SFTPServerService extends IntentService {
    SshServer sshd;
    Config config = Config.getInstance();
    UserDao dao;
    NotificationManager manager;

    public SFTPServerService() {
        super("SFTPServerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        dao=UserDatabaseSingleton.getInstance(this).getUserDao();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        有问题，关于keystore的，待解决
        System.setProperty("user.home", Environment.getExternalStorageDirectory().getAbsolutePath());
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(config.port);
        sshd.setHost("0.0.0.0");
//        sshd.setKeyPairProvider(new CustomKeyPairProvider(this));
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get(new File(getExternalFilesDir(null), "key").toURI())));

        sshd.setSignatureFactories(new ArrayList<>(BuiltinSignatures.VALUES));

        sshd.setPasswordAuthenticator((username, password, session) -> {
            var pwd=dao.getPwdFromUser(username);
            if (pwd==null){
//                Log.i("user", "no exist");
                return false;
            }
            var enable=dao.getEnableStatusFromUser(username);
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
                var home=dao.getHomeFromUser(user);
                return home==null?null:Paths.get(home);
            }

            @Override
            public FileSystem createFileSystem(SessionContext session) throws IOException {
                var user = session.getUsername();
                var home=dao.getHomeFromUser(user);
                return home==null?null:new VirtualFileSystemFactory(Paths.get(home)).createFileSystem(session);
            }
        });
        sshd.setSubsystemFactories(Arrays.asList(new SftpSubsystemFactory()));

        Notification n = createForegroundNotification();
        startForeground(1, n);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        try {
            sshd.close();
            sshd = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        synchronized (config) {
            config.is_running = false;
        }
        EventBus.getDefault().post(new SFTPStatusSwitchEvent());
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
            notification_builder = new Notification.Builder(this, channel_id);
        } else {
            notification_builder = new Notification.Builder(this);
        }
        notification_builder.setSmallIcon(R.mipmap.ic_launcher);
        notification_builder.setContentTitle("Leaf SFTP Server");
        notification_builder.setContentText("running");
        return notification_builder.build();
    }
}

//未使用的KeyPairProvider类。
//在整个项目中并没有什么卵用，用起来会导致ssh服务端跑不起来
//但其具体实现过程仍具有参考意义
@Deprecated
class CustomKeyPairProvider implements KeyPairProvider {
    private final List<KeyPair> keyPairs = new ArrayList<>();
//    private static final String KEYSTORE_PASSWORD = "leafoct";
    private static final String KEY_ALIAS = "leafoct";
//    private static final String KEY_PASSWORD = "leafoct";

    public CustomKeyPairProvider(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 1);

//            Android 6 已弃用
//            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
//                    .setAlias(KEY_ALIAS)
//                    .setSubject(new X500Principal("CN=Sample Key, O=Sample Organization"))
//                    .setSerialNumber(BigInteger.ONE)
//                    .setStartDate(start.getTime())
//                    .setEndDate(end.getTime())
//                    .build();

            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setKeySize(2048)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setRandomizedEncryptionRequired(false)
                    .build();

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(spec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();


            keyPairs.add(keyPair);
//            PublicKey publicKey = keyPair.getPublic();
//            PrivateKey privateKey = keyPair.getPrivate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
        // TODO Auto-generated method stub
        return keyPairs;
    }
}