package cn.leaf.leafsftp;

import android.app.Application;
import android.content.Context;

public class LeafApplication extends Application {
    private static Context c;

    @Override
    public void onCreate() {
        super.onCreate();
        c=getApplicationContext();
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }
    public static Context getContext(){
        return c;
    }
}
