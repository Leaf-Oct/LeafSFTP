package cn.leaf.wavingleaf;

import android.app.Application;
import android.content.Context;

//完成初始化工作
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
