package com.malata.superclean.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by xuxiantao on 2015/9/12.
 */
public class BaseApplication extends Application {

    private static BaseApplication mInstance;
    private Context mContext;

    public static BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        MyCrashHandler myCrashHandler = MyCrashHandler.getInstance();
        myCrashHandler.init(getApplicationContext());
        Thread.currentThread().setUncaughtExceptionHandler(myCrashHandler);
    }

    @Override
    public void onLowMemory() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onLowMemory();
    }
}
