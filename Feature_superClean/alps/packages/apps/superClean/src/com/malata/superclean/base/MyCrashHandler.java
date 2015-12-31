package com.malata.superclean.base;

import android.content.Context;

/**
 * Created by xuxiantao on 2015/9/12.
 */
public class MyCrashHandler implements Thread.UncaughtExceptionHandler {

    private static MyCrashHandler myCrashHandler;
    private Context context;

    private MyCrashHandler() {

    }

    public static synchronized MyCrashHandler getInstance() {
        if(myCrashHandler == null) {
            myCrashHandler = new MyCrashHandler();
        }
        return myCrashHandler;
    }

    public void init(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
