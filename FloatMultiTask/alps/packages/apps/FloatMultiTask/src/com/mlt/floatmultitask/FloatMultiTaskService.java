package com.mlt.floatmultitask;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * filename:FloatMultiTaskService.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-6
 * author: laiyang
 *
 * The class is service, is to create float button and remove all float windows
 *
 * Modification History
 * -----------------------------------
 *
 * -----------------------------------
 */
public class FloatMultiTaskService extends Service {

    public FloatMultiTaskService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {// start float button
        MultiTaskManager.createFLoatButton(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {// remove all float button
        Intent stop = new Intent(getApplicationContext(), BackTaskService.class);
        stopService(stop);
        MultiTaskManager.removeFloatButton(getApplicationContext());
        MultiTaskManager.removeMainWindow(getApplicationContext());
        MultiTaskManager.removeMusicWindow(getApplicationContext());
        MultiTaskManager.removeNoteWindow(getApplicationContext());
        MultiTaskManager.removeVideoWindow(getApplicationContext());
        super.onDestroy();
    }

}
