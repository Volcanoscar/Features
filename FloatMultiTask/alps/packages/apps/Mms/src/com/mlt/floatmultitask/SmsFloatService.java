package com.mlt.floatmultitask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

public class SmsFloatService extends Service {

    private WindowManager mWindowManger;

    public SmsFloatService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SmsFloatManager.createMsgWindow(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        SmsFloatManager.removeMsgWindow(getApplicationContext());
        super.onDestroy();
    }

    private WindowManager getWindowManager(Context context) {
        if(null == mWindowManger) {
            mWindowManger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManger;
    }
}

