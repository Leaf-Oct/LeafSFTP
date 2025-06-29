package cn.leaf.wavingleaf;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

//完成初始化工作
public class LeafApplication extends Application {
    private static Context c;

    @Override
    public void onCreate() {
        super.onCreate();
        c=getApplicationContext();
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("user.home", Environment.getExternalStorageDirectory().getAbsolutePath());
        System.setProperty("org.apache.sshd.security.provider.BC.enabled", "false");
        System.setProperty("org.apache.sshd.security.registrars", "none");
    }
    public static Context getContext(){
        return c;
    }
}
