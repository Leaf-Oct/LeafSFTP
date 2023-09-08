package cn.leaf.leafftp;

import android.app.Application;
import android.content.Context;

public class LeafApplication extends Application {
    private static Context c;

    @Override
    public void onCreate() {
        super.onCreate();
        c=getApplicationContext();
    }
    public static Context getContext(){
        return c;
    }
}
